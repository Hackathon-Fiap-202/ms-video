package com.nextimefood.msvideo.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("Domain Exception Tests")
class ExceptionTest {

    @Test
    @DisplayName("VideoNotFoundException should include videoKey in message and expose it via getter")
    void shouldCreateVideoNotFoundException() {
        // Arrange / Act
        final var ex = new VideoNotFoundException("key-abc");

        // Assert
        assertEquals("key-abc", ex.getVideoKey());
        assertEquals("Vídeo não encontrado com a chave: key-abc", ex.getMessage());
    }

    @Test
    @DisplayName("InvalidFileException should carry the provided message")
    void shouldCreateInvalidFileException() {
        // Arrange / Act
        final var ex = new InvalidFileException("Arquivo vazio");

        // Assert
        assertEquals("Arquivo vazio", ex.getMessage());
    }

    @Test
    @DisplayName("VideoUploadException should support message-only constructor")
    void shouldCreateVideoUploadExceptionWithMessage() {
        // Arrange / Act
        final var ex = new VideoUploadException("upload falhou");

        // Assert
        assertEquals("upload falhou", ex.getMessage());
    }

    @Test
    @DisplayName("VideoUploadException should support message and cause constructor")
    void shouldCreateVideoUploadExceptionWithCause() {
        // Arrange
        final var cause = new RuntimeException("root cause");

        // Act
        final var ex = new VideoUploadException("upload falhou", cause);

        // Assert
        assertEquals("upload falhou", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    @DisplayName("MessagePublishException should carry message and cause")
    void shouldCreateMessagePublishException() {
        // Arrange
        final var cause = new RuntimeException("sqs down");

        // Act
        final var ex = new MessagePublishException("publish error", cause);

        // Assert
        assertEquals("publish error", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    @DisplayName("MessageProcessingException should carry message and cause")
    void shouldCreateMessageProcessingException() {
        // Arrange
        final var cause = new RuntimeException("json bad");

        // Act
        final var ex = new MessageProcessingException("processing error", cause);

        // Assert
        assertEquals("processing error", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}
