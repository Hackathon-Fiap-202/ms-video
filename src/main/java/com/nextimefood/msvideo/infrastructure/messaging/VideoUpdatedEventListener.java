package com.nextimefood.msvideo.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextimefood.msvideo.application.dto.VideoStatusEventDTO;
import com.nextimefood.msvideo.application.usecases.VideoStatusUpdateUseCase;
import com.nextimefood.msvideo.domain.exception.MessageProcessingException;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VideoUpdatedEventListener {

    private static final Logger logger = LoggerFactory.getLogger(VideoUpdatedEventListener.class);

    private final ObjectMapper objectMapper;
    private final VideoStatusUpdateUseCase videoStatusUpdateUseCase;

    public VideoUpdatedEventListener(ObjectMapper objectMapper, VideoStatusUpdateUseCase videoStatusUpdateUseCase) {
        this.objectMapper = objectMapper;
        this.videoStatusUpdateUseCase = videoStatusUpdateUseCase;
    }

    @SqsListener("${spring.cloud.sqs.queues.video-updated-event}")
    public void listen(String message) {
        logger.info("Received message from video-updated-event queue");
        logger.debug("Message content: {}", message);

        try {
            final var event = deserializeMessage(message);
            
            logger.info("Processing video updated event for videoKey: {}", event.getVideoKey());

            videoStatusUpdateUseCase.processVideoStatusUpdate(event);

            logger.info("Successfully processed video updated event for videoKey: {}", event.getVideoKey());

        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize message from video-updated-event queue. Invalid JSON format: {}", message, e);
            throw new MessageProcessingException("Erro ao deserializar mensagem do evento de vídeo atualizado", e);
        } catch (MessageProcessingException e) {
            logger.error("Message processing exception occurred: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error processing video updated event message: {}", message, e);
            throw new MessageProcessingException("Erro inesperado ao processar evento de vídeo atualizado", e);
        }
    }

    private VideoStatusEventDTO deserializeMessage(String message) throws JsonProcessingException {
        logger.debug("Deserializing message to VideoStatusEventDTO");
        return objectMapper.readValue(message, VideoStatusEventDTO.class);
    }
}
