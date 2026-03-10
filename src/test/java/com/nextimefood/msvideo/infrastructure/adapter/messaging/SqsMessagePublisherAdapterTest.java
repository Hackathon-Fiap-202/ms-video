package com.nextimefood.msvideo.infrastructure.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextimefood.msvideo.domain.exception.MessagePublishException;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SqsMessagePublisherAdapter Tests")
class SqsMessagePublisherAdapterTest {

    @Mock
    private SqsTemplate sqsTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SqsMessagePublisherAdapter adapter;

    @Nested
    @DisplayName("Successful publish")
    class SuccessPathTests {

        @Test
        @DisplayName("Should publish message to queue successfully")
        void shouldPublishMessageSuccessfully() throws JsonProcessingException {
            // Arrange
            final var payload = new Object();
            when(objectMapper.writeValueAsString(payload)).thenReturn("{\"key\":\"value\"}");

            // Act
            adapter.publish("test-queue", payload);

            // Assert
            verify(sqsTemplate).send(any(Consumer.class));
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorPathTests {

        @Test
        @DisplayName("Should throw MessagePublishException when serialization fails")
        void shouldThrowMessagePublishExceptionWhenSerializationFails() throws JsonProcessingException {
            // Arrange
            final var payload = new Object();
            when(objectMapper.writeValueAsString(payload))
                    .thenThrow(new com.fasterxml.jackson.core.JsonGenerationException("serialize error", (com.fasterxml.jackson.core.JsonGenerator) null));

            // Act & Assert
            assertThrows(MessagePublishException.class, () -> adapter.publish("test-queue", payload));
        }

        @Test
        @DisplayName("Should rethrow MessagePublishException from SqsTemplate")
        void shouldRethrowMessagePublishException() throws JsonProcessingException {
            // Arrange
            final var payload = new Object();
            when(objectMapper.writeValueAsString(payload)).thenReturn("{\"key\":\"value\"}");
            when(sqsTemplate.send(any(Consumer.class)))
                    .thenThrow(new MessagePublishException("sqs error", new RuntimeException()));

            // Act & Assert
            assertThrows(MessagePublishException.class, () -> adapter.publish("test-queue", payload));
        }

        @Test
        @DisplayName("Should wrap unexpected exception in MessagePublishException")
        void shouldWrapUnexpectedExceptionInMessagePublishException() throws JsonProcessingException {
            // Arrange
            final var payload = new Object();
            when(objectMapper.writeValueAsString(payload)).thenReturn("{\"key\":\"value\"}");
            when(sqsTemplate.send(any(Consumer.class)))
                    .thenThrow(new RuntimeException("unexpected error"));

            // Act & Assert
            assertThrows(MessagePublishException.class, () -> adapter.publish("test-queue", payload));
        }
    }
}
