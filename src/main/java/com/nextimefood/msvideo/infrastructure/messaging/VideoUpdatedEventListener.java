package com.nextimefood.msvideo.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextimefood.msvideo.application.dto.VideoUpdatedEvent;
import com.nextimefood.msvideo.application.ports.outgoing.MessagePublisherPort;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VideoUpdatedEventListener {

    private static final Logger logger = LoggerFactory.getLogger(VideoUpdatedEventListener.class);

    private final ObjectMapper objectMapper;
    private final VideoRepositoryPort repository;
    private final MessagePublisherPort publisher;

    @org.springframework.beans.factory.annotation.Value("${spring.cloud.sqs.queues.video-process-event}")
    private String videoProcessedEventQueue;

    public VideoUpdatedEventListener(ObjectMapper objectMapper, VideoRepositoryPort repository, MessagePublisherPort publisher) {
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.publisher = publisher;
    }

    @SqsListener("${spring.cloud.sqs.queues.video-updated-event}")
    public void listen(String message) {
        try {
            logger.info("Received message from video-updated-event queue: {}", message);

            VideoUpdatedEvent event = objectMapper.readValue(message, VideoUpdatedEvent.class);

            logger.info("Processing video updated event: {}", event);

            processVideoUpdatedEvent(event);

            logger.info("Successfully processed video updated event for videoId: {}", event.getVideoId());

        } catch (Exception e) {
            logger.error("Error processing video updated event message: {}", message, e);
            throw new RuntimeException("Failed to process video updated event", e);
        }
    }

    private void processVideoUpdatedEvent(VideoUpdatedEvent event) {
        repository.findByKey(event.getVideoId()).ifPresentOrElse(video -> {
            video.setStatus(event.getStatus());
            repository.save(video);
            publisher.publish(videoProcessedEventQueue, event);
        }, () -> logger.error("Video not found with key: {}", event.getVideoId()));
    }
}
