package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.VideoUploadPresignRequest;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.application.ports.outgoing.VideoStoragePort;
import com.nextimefood.msvideo.domain.exception.InvalidFileException;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VideoUploadPresignUseCase Tests")
class VideoUploadPresignUseCaseTest {

    @Mock
    private VideoStoragePort storage;

    @Mock
    private VideoRepositoryPort repository;

    @InjectMocks
    private VideoUploadPresignUseCase useCase;

    private static final String BUCKET = "test-bucket";
    private static final String PREFIX = "video-input-storage/";
    private static final String PRESIGNED_URL = "https://s3.amazonaws.com/test-bucket/key?X-Amz-Signature=abc";

    @Nested
    @DisplayName("Successful presign")
    class SuccessPathTests {

        @Test
        @DisplayName("Should return presign response with key and URL")
        void shouldReturnPresignResponseWithKeyAndUrl() {
            // Arrange
            ReflectionTestUtils.setField(useCase, "bucketName", BUCKET);
            ReflectionTestUtils.setField(useCase, "inputPrefix", PREFIX);
            final var request = new VideoUploadPresignRequest("video.mp4", "video/mp4");
            final var savedDoc = new VideoDocument();
            when(storage.generatePresignedPutUrl(eq(BUCKET), any(String.class), any(Duration.class)))
                    .thenReturn(PRESIGNED_URL);
            when(repository.save(any(VideoDocument.class))).thenReturn(savedDoc);

            // Act
            final var response = useCase.presign(request, "user-123");

            // Assert
            assertNotNull(response);
            assertEquals(PRESIGNED_URL, response.getUploadUrl());
            assertEquals("15 minutes", response.getExpiresIn());
            assertTrue(response.getKey().startsWith(PREFIX));
            assertTrue(response.getKey().endsWith(".mp4"));
        }

        @Test
        @DisplayName("Should save VideoDocument with status RECEIVED")
        void shouldSaveVideoDocumentWithStatusReceived() {
            // Arrange
            ReflectionTestUtils.setField(useCase, "bucketName", BUCKET);
            ReflectionTestUtils.setField(useCase, "inputPrefix", PREFIX);
            final var request = new VideoUploadPresignRequest("clip.mp4", "video/mp4");
            final var savedDoc = new VideoDocument();
            when(storage.generatePresignedPutUrl(any(), any(), any())).thenReturn(PRESIGNED_URL);
            when(repository.save(any(VideoDocument.class))).thenReturn(savedDoc);

            final ArgumentCaptor<VideoDocument> captor = ArgumentCaptor.forClass(VideoDocument.class);

            // Act
            useCase.presign(request, "user-456");

            // Assert
            verify(repository).save(captor.capture());
            final VideoDocument saved = captor.getValue();
            assertEquals(BUCKET, saved.getBucket());
            assertEquals("clip.mp4", saved.getOriginalFilename());
            assertEquals("user-456", saved.getCognitoUserId());
        }
    }

    @Nested
    @DisplayName("Validation failure")
    class ValidationTests {

        @Test
        @DisplayName("Should throw InvalidFileException when filename is null")
        void shouldThrowWhenFilenameIsNull() {
            // Arrange
            ReflectionTestUtils.setField(useCase, "bucketName", BUCKET);
            ReflectionTestUtils.setField(useCase, "inputPrefix", PREFIX);
            final var request = new VideoUploadPresignRequest(null, "video/mp4");

            // Act & Assert
            assertThrows(InvalidFileException.class, () -> useCase.presign(request, "user-123"));
        }

        @Test
        @DisplayName("Should throw InvalidFileException when filename is blank")
        void shouldThrowWhenFilenameIsBlank() {
            // Arrange
            ReflectionTestUtils.setField(useCase, "bucketName", BUCKET);
            ReflectionTestUtils.setField(useCase, "inputPrefix", PREFIX);
            final var request = new VideoUploadPresignRequest("  ", "video/mp4");

            // Act & Assert
            assertThrows(InvalidFileException.class, () -> useCase.presign(request, "user-123"));
        }
    }
}
