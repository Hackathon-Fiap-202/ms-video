package com.nextimefood.msvideo.infrastructure.exception;

import com.nextimefood.msvideo.domain.exception.InvalidFileException;
import com.nextimefood.msvideo.domain.exception.VideoNotFoundException;
import com.nextimefood.msvideo.domain.exception.VideoUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("VideoNotFoundException handling")
    class VideoNotFoundExceptionTests {

        @Test
        @DisplayName("Should return 404 with problem detail when video not found")
        void shouldReturn404WhenVideoNotFound() {
            // Arrange
            final var ex = new VideoNotFoundException("video-key-123");

            // Act
            final ProblemDetail result = handler.handleVideoNotFoundException(ex);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus());
            assertEquals("Vídeo Não Encontrado", result.getTitle());
            assertNotNull(result.getProperties());
            assertEquals("video-key-123", result.getProperties().get("videoKey"));
            assertNotNull(result.getProperties().get("timestamp"));
        }
    }

    @Nested
    @DisplayName("InvalidFileException handling")
    class InvalidFileExceptionTests {

        @Test
        @DisplayName("Should return 400 with problem detail when file is invalid")
        void shouldReturn400WhenFileIsInvalid() {
            // Arrange
            final var ex = new InvalidFileException("Arquivo vazio");

            // Act
            final ProblemDetail result = handler.handleInvalidFileException(ex);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
            assertEquals("Arquivo Inválido", result.getTitle());
            assertEquals("Arquivo vazio", result.getDetail());
            assertNotNull(result.getProperties());
            assertNotNull(result.getProperties().get("timestamp"));
        }
    }

    @Nested
    @DisplayName("VideoUploadException handling")
    class VideoUploadExceptionTests {

        @Test
        @DisplayName("Should return 500 with problem detail when upload fails")
        void shouldReturn500WhenUploadFails() {
            // Arrange
            final var ex = new VideoUploadException("Erro ao fazer upload", new RuntimeException("cause"));

            // Act
            final ProblemDetail result = handler.handleVideoUploadException(ex);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
            assertEquals("Erro no Upload", result.getTitle());
            assertNotNull(result.getProperties());
            assertNotNull(result.getProperties().get("timestamp"));
        }
    }

    @Nested
    @DisplayName("MaxUploadSizeExceededException handling")
    class MaxUploadSizeExceededExceptionTests {

        @Test
        @DisplayName("Should return 413 with problem detail when file too large")
        void shouldReturn413WhenFileTooLarge() {
            // Arrange
            final var ex = new MaxUploadSizeExceededException(10L);

            // Act
            final ProblemDetail result = handler.handleMaxUploadSizeExceededException(ex);

            // Assert
            assertEquals(HttpStatus.PAYLOAD_TOO_LARGE.value(), result.getStatus());
            assertEquals("Arquivo Muito Grande", result.getTitle());
            assertNotNull(result.getProperties());
            assertNotNull(result.getProperties().get("timestamp"));
        }
    }

    @Nested
    @DisplayName("IllegalArgumentException handling")
    class IllegalArgumentExceptionTests {

        @Test
        @DisplayName("Should return 400 with problem detail for illegal argument")
        void shouldReturn400ForIllegalArgument() {
            // Arrange
            final var ex = new IllegalArgumentException("argumento inválido");

            // Act
            final ProblemDetail result = handler.handleIllegalArgumentException(ex);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
            assertEquals("Requisição Inválida", result.getTitle());
            assertEquals("argumento inválido", result.getDetail());
            assertNotNull(result.getProperties());
            assertNotNull(result.getProperties().get("timestamp"));
        }
    }

    @Nested
    @DisplayName("Generic Exception handling")
    class GenericExceptionTests {

        @Test
        @DisplayName("Should return 500 with problem detail for unexpected exception")
        void shouldReturn500ForUnexpectedException() {
            // Arrange
            final var ex = new Exception("erro inesperado");

            // Act
            final ProblemDetail result = handler.handleGenericException(ex);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
            assertEquals("Erro Interno", result.getTitle());
            assertNotNull(result.getProperties());
            assertNotNull(result.getProperties().get("timestamp"));
        }
    }
}
