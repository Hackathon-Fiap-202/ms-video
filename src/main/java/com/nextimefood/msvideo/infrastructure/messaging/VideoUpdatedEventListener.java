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

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoUpdatedEventListener.class);

    private final ObjectMapper objectMapper;
    private final VideoStatusUpdateUseCase videoStatusUpdateUseCase;

    public VideoUpdatedEventListener(ObjectMapper objectMapper, VideoStatusUpdateUseCase videoStatusUpdateUseCase) {
        this.objectMapper = objectMapper;
        this.videoStatusUpdateUseCase = videoStatusUpdateUseCase;
    }

    @SqsListener("${spring.cloud.sqs.queues.video-updated-event}")
    public void listen(String message) {
        LOGGER.info("Received message from video-updated-event queue");
        LOGGER.debug("Message content received");

        try {
            final var event = deserializeMessage(message);
            
            LOGGER.info("Processing video updated event");

            videoStatusUpdateUseCase.processVideoStatusUpdate(event);

            LOGGER.info("Successfully processed video updated event");

        } catch (JsonProcessingException e) {
            throw new MessageProcessingException("Erro ao deserializar mensagem do evento de vídeo atualizado", e);
        } catch (MessageProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new MessageProcessingException("Erro inesperado ao processar evento de vídeo atualizado", e);
        }
    }

    private VideoStatusEventDTO deserializeMessage(String message) throws JsonProcessingException {
        LOGGER.debug("Deserializing message to VideoStatusEventDTO");
        return objectMapper.readValue(message, VideoStatusEventDTO.class);
    }
}
