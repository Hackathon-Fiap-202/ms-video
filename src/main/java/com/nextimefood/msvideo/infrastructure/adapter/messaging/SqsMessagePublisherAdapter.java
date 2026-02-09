package com.nextimefood.msvideo.infrastructure.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextimefood.msvideo.application.ports.outgoing.MessagePublisherPort;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class SqsMessagePublisherAdapter implements MessagePublisherPort {

    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;

    public SqsMessagePublisherAdapter(SqsTemplate sqsTemplate, ObjectMapper objectMapper) {
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(String queueName, Object payload) {
        String body = toJson(payload);
        sqsTemplate.send(to -> to
                .queue(queueName)
                .payload(body)
        );
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar payload para SQS", e);
        }
    }
}
