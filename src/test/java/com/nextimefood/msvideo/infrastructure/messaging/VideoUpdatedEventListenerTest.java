package com.nextimefood.msvideo.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextimefood.msvideo.application.dto.VideoStatusEventDTO;
import com.nextimefood.msvideo.application.usecases.VideoStatusUpdateUseCase;
import com.nextimefood.msvideo.domain.ProcessStatus;
import com.nextimefood.msvideo.domain.exception.MessageProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VideoUpdatedEventListener Tests")
class VideoUpdatedEventListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private VideoStatusUpdateUseCase videoStatusUpdateUseCase;

    @InjectMocks
    private VideoUpdatedEventListener listener;

    @Nested
    @DisplayName("Successful message processing")
    class SuccessPathTests {

        @Test
        @DisplayName("Should deserialize and process message successfully")
        void shouldProcessMessageSuccessfully() throws JsonProcessingException {
            // Arrange
            final var message = "{\"videoKey\":\"test-key\",\"success\":true}";
            final var event = new VideoStatusEventDTO("test-key", true, ProcessStatus.PROCESSED, 30, 1024L, "2026-01-01");
            when(objectMapper.readValue(message, VideoStatusEventDTO.class)).thenReturn(event);

            // Act
            listener.listen(message);

            // Assert
            verify(videoStatusUpdateUseCase).processVideoStatusUpdate(event);
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorPathTests {

        @Test
        @DisplayName("Should throw MessageProcessingException when JSON is invalid")
        void shouldThrowMessageProcessingExceptionWhenJsonIsInvalid() throws JsonProcessingException {
            // Arrange
            final var message = "invalid-json";
            when(objectMapper.readValue(anyString(), any(Class.class)))
                    .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "bad json"));

            // Act & Assert
            assertThrows(MessageProcessingException.class, () -> listener.listen(message));
        }

        @Test
        @DisplayName("Should rethrow MessageProcessingException from use case")
        void shouldRethrowMessageProcessingExceptionFromUseCase() throws JsonProcessingException {
            // Arrange
            final var message = "{\"videoKey\":\"test-key\"}";
            final var event = new VideoStatusEventDTO("test-key", true, ProcessStatus.PROCESSED, 0, 0L, null);
            when(objectMapper.readValue(message, VideoStatusEventDTO.class)).thenReturn(event);
            doThrow(new MessageProcessingException("processing error", new RuntimeException()))
                    .when(videoStatusUpdateUseCase).processVideoStatusUpdate(any());

            // Act & Assert
            assertThrows(MessageProcessingException.class, () -> listener.listen(message));
        }

        @Test
        @DisplayName("Should wrap unexpected exceptions in MessageProcessingException")
        void shouldWrapUnexpectedExceptionInMessageProcessingException() throws JsonProcessingException {
            // Arrange
            final var message = "{\"videoKey\":\"test-key\"}";
            final var event = new VideoStatusEventDTO("test-key", true, ProcessStatus.PROCESSED, 0, 0L, null);
            when(objectMapper.readValue(message, VideoStatusEventDTO.class)).thenReturn(event);
            doThrow(new RuntimeException("unexpected"))
                    .when(videoStatusUpdateUseCase).processVideoStatusUpdate(any());

            // Act & Assert
            assertThrows(MessageProcessingException.class, () -> listener.listen(message));
        }
    }
}
