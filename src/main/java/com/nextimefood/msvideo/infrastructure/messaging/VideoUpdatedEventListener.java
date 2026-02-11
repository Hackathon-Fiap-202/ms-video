package com.nextimefood.msvideo.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextimefood.msvideo.application.dto.VideoUpdatedEvent;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SQS Listener for video-updated-event queue
 */
@Component
public class VideoUpdatedEventListener {

    private static final Logger logger = LoggerFactory.getLogger(VideoUpdatedEventListener.class);

    private final ObjectMapper objectMapper;

    public VideoUpdatedEventListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SqsListener("${spring.cloud.sqs.queues.video-updated-event}")
    public void listen(String message) {
        try {
            logger.info("Received message from video-updated-event queue: {}", message);

            VideoUpdatedEvent event = objectMapper.readValue(message, VideoUpdatedEvent.class);

            logger.info("Processing video updated event: {}", event);

            // TODO: Add your business logic here
            processVideoUpdatedEvent(event);

            logger.info("Successfully processed video updated event for videoId: {}", event.getVideoId());

        } catch (Exception e) {
            logger.error("Error processing video updated event message: {}", message, e);
            throw new RuntimeException("Failed to process video updated event", e);
        }
    }

    private void processVideoUpdatedEvent(VideoUpdatedEvent event) {
        // TODO: Implement the business logic for processing video updated events
        // For example:
        // - Update video status in database
        // - Send notifications
        // - Trigger other workflows
        logger.info("Processing event for video: {} with status: {}",
                event.getVideoId(), event.getStatus());
    }
}
