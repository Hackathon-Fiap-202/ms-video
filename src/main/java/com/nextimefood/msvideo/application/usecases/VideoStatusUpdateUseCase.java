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

    @Value("${spring.cloud.sqs.queues.video-process-event}")
    private String videoProcessedEventQueue;

    public VideoStatusUpdateUseCase(VideoRepositoryPort repository, MessagePublisherPort publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public void processVideoStatusUpdate(VideoStatusEventDTO event) {
        LOGGER.info("Processing video status update");
        
        final var videoOpt = repository.findByKey(event.getVideoKey());
        
        if (videoOpt.isEmpty()) {
            LOGGER.error("Video not found from event");
            return;
        }
        
        final var video = videoOpt.get();
        
        if (event.isSuccess()) {
            handleSuccessfulProcessing(event, video);
        } else {
            handleFailedProcessing(event, video);
        }
    }

    private void handleSuccessfulProcessing(VideoStatusEventDTO event, VideoDocument video) {
        LOGGER.info("Video processing succeeded");

        video.setStatus(event.getStatus());
        video.setFrameCount(event.getFrameCount());
        video.setArchiveSize(event.getArchiveSize());
        video.setProcessedKey(deriveZipFilename(event.getVideoKey()));
        repository.save(video);

        final var lambdaEvent = ProcessedVideoEvent.builder()
                .cognitoUserId(video.getCognitoUserId())
                .keyName(video.getProcessedKey())
                .status(event.getStatus().name())
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
