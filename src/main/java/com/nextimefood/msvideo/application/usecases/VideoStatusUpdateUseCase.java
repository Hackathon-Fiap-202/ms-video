package com.nextimefood.msvideo.application.usecases;

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
        LOGGER.info("Processing video status update for videoKey: {}, success: {}", event.getVideoKey(), event.isSuccess());
        
        final var videoOpt = repository.findByKey(event.getVideoKey());
        
        if (videoOpt.isEmpty()) {
            LOGGER.error("Video not found with key: {}", event.getVideoKey());
            return;
        }
        
        final var video = videoOpt.get();
        
        try {
            if (event.isSuccess()) {
                handleSuccessfulProcessing(event, video);
            } else {
                handleFailedProcessing(event, video);
            }
        } catch (Exception e) {
            LOGGER.error("Error updating video status for key: {}", event.getVideoKey(), e);
            throw e;
        }
    }

    private void handleSuccessfulProcessing(VideoStatusEventDTO event, VideoDocument video) {
        LOGGER.info("Video processing succeeded for key: {}", event.getVideoKey());
        
        video.setStatus(event.getStatus());
        video.setFrameCount(event.getFrameCount());
        video.setArchiveSize(event.getArchiveSize());
        repository.save(video);
        
        final var lambdaEvent = com.nextimefood.msvideo.application.dto.ProcessedVideoEvent.builder()
                .cognito_user_id(video.getCognitoUserId())
                .key_name(event.getVideoKey())
                .status(event.getStatus().name())
                .build();

        publisher.publish(videoProcessedEventQueue, lambdaEvent);
        
        LOGGER.info("Video updated and event published for successful processing to queue: {}", videoProcessedEventQueue);
    }

    private void handleFailedProcessing(VideoStatusEventDTO event, VideoDocument video) {
        LOGGER.warn("Video processing failed for key: {}", event.getVideoKey());
        
        video.setStatus(event.getStatus());
        repository.save(video);
        
        LOGGER.info("Video status updated to {} for failed processing", event.getStatus());
    }
}
