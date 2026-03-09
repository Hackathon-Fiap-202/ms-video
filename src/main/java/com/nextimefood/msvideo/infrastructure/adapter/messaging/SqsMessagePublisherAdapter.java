package com.nextimefood.msvideo.infrastructure.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextimefood.msvideo.application.ports.outgoing.MessagePublisherPort;
import com.nextimefood.msvideo.domain.exception.MessagePublishException;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class SqsMessagePublisherAdapter implements MessagePublisherPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsMessagePublisherAdapter.class);

    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;

    public SqsMessagePublisherAdapter(SqsTemplate sqsTemplate, ObjectMapper objectMapper) {
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(String queueName, Object payload) {
        LOGGER.info("Publishing message to queue: {}", queueName);
        LOGGER.debug("Message payload type: {}", payload.getClass().getSimpleName());

        try {
            final var body = toJson(payload);
            LOGGER.info("Serialized message body: {}", body);

            sqsTemplate.send(to -> to
                    .queue(queueName)
                    .payload(body)
            );

            LOGGER.info("Message published successfully to queue: {}", queueName);
        } catch (MessagePublishException e) {
            LOGGER.error("Failed to publish message to queue: {}", queueName, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error while publishing message to queue: {}", queueName, e);
            throw new MessagePublishException("Erro inesperado ao publicar mensagem na fila " + queueName, e);
        }
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize payload to JSON: {}", payload.getClass().getSimpleName(), e);
            throw new MessagePublishException("Erro ao serializar payload para JSON", e);
        }
    }
}
