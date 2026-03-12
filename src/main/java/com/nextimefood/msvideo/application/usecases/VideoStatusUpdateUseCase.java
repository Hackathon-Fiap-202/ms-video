package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.ProcessedVideoEvent;
import com.nextimefood.msvideo.application.dto.VideoStatusEventDTO;
import com.nextimefood.msvideo.application.ports.outgoing.MessagePublisherPort;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VideoStatusUpdateUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoStatusUpdateUseCase.class);

    private final VideoRepositoryPort repository;
    private final MessagePublisherPort publisher;
    private final VideoDownloadUseCase videoDownloadUseCase;

    @Value("${spring.cloud.sqs.queues.video-process-event}")
    private String videoProcessedEventQueue;

    public VideoStatusUpdateUseCase(VideoRepositoryPort repository, MessagePublisherPort publisher, VideoDownloadUseCase videoDownloadUseCase) {
        this.repository = repository;
        this.publisher = publisher;
        this.videoDownloadUseCase = videoDownloadUseCase;
    }

    public void processVideoStatusUpdate(VideoStatusEventDTO event) {
        LOGGER.info("Processing video status update, status={}", event.getStatus());

        final var videoOpt = repository.findByKey(event.getVideoKey());

        if (videoOpt.isEmpty()) {
            LOGGER.error("Video not found from event, videoKey={}", event.getVideoKey());
            return;
        }

        final var video = videoOpt.get();

        switch (event.getStatus()) {
            case PROCESSING -> handleProcessingStatus(event, video);
            case PROCESSED  -> handleSuccessfulProcessing(event, video);
            case FAILED     -> handleFailedProcessing(event, video);
            default -> LOGGER.warn("Unknown status received, status={}", event.getStatus());
        }
    }

    private void handleProcessingStatus(VideoStatusEventDTO event, VideoDocument video) {
        LOGGER.info("Video processing started, videoKey={}", event.getVideoKey());

        video.setStatus(event.getStatus());
        repository.save(video);

        LOGGER.info("Video status updated to PROCESSING, videoKey={}", event.getVideoKey());
    }

    private void handleSuccessfulProcessing(VideoStatusEventDTO event, VideoDocument video) {
        LOGGER.info("Video processing succeeded");

        video.setStatus(event.getStatus());
        video.setFrameCount(event.getFrameCount());
        video.setArchiveSize(event.getArchiveSize());
        video.setProcessedKey(deriveZipFilename(event.getVideoKey()));
        repository.save(video);

        final var downloadResponse = videoDownloadUseCase.generateDownloadUrl(video.getProcessedKey());

        final var lambdaEvent = ProcessedVideoEvent.builder()
                .cognitoUserId(video.getCognitoUserId())
                .keyName(video.getProcessedKey())
                .status(event.getStatus().name())
                .downloadUrl(downloadResponse.getDownloadUrl())
                .build();

        publisher.publish(videoProcessedEventQueue, lambdaEvent);

        LOGGER.info("Video updated and event published for successful processing to queue");
    }

    private void handleFailedProcessing(VideoStatusEventDTO event, VideoDocument video) {
        LOGGER.warn("Video processing failed");

        video.setStatus(event.getStatus());
        repository.save(video);

        LOGGER.info("Video status updated for failed processing");
    }

    private String deriveZipFilename(String inputKey) {
        final String filename = inputKey.substring(inputKey.lastIndexOf('/') + 1);
        final String nameWithoutExtension = filename.contains(".")
                ? filename.substring(0, filename.lastIndexOf('.'))
                : filename;
        return nameWithoutExtension + ".zip";
    }
}
