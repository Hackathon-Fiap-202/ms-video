package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.VideoStatusResponse;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.domain.ProcessStatus;
import com.nextimefood.msvideo.domain.exception.VideoNotFoundException;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VideoStatusUseCase Tests")
class VideoStatusUseCaseTest {

    @Mock
    private VideoRepositoryPort repository;

    @InjectMocks
    private VideoStatusUseCase videoStatusUseCase;

    private VideoDocument videoDocument;

    private static final String TEST_KEY = "video-input-storage/start-process/abc123.mp4";
    private static final String TEST_ID = "doc-id-001";
    private static final String TEST_FILENAME = "my-video.mp4";
    private static final Instant TEST_CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant TEST_UPDATED_AT = Instant.parse("2026-01-01T11:00:00Z");

    @BeforeEach
    void setUp() {
        videoDocument = new VideoDocument();
        videoDocument.setId(TEST_ID);
        videoDocument.setKey(TEST_KEY);
        videoDocument.setOriginalFilename(TEST_FILENAME);
        videoDocument.setBucket("test-bucket");
        videoDocument.setContentType("video/mp4");
        videoDocument.setSize(2048L);
        videoDocument.setCreatedAt(TEST_CREATED_AT);
        videoDocument.setUpdatedAt(TEST_UPDATED_AT);
    }

    @Nested
    @DisplayName("getStatus() — success paths")
    class SuccessPathTests {

        @Test
        @DisplayName("Should return status RECEIVED when video is newly uploaded")
        void shouldReturnStatusReceivedWhenVideoIsNewlyUploaded() {
            // Arrange
            final String searchSuffix = TEST_KEY.substring(TEST_KEY.lastIndexOf("/") + 1);
            videoDocument.setStatus(ProcessStatus.RECEIVED);
            when(repository.findByKeyEndingWith(searchSuffix)).thenReturn(Optional.of(videoDocument));

            // Act
            final VideoStatusResponse response = videoStatusUseCase.getStatus(TEST_KEY);

            // Assert
            assertNotNull(response);
            assertEquals(TEST_ID, response.getVideoId());
            assertEquals(TEST_KEY, response.getKey());
            assertEquals(TEST_FILENAME, response.getOriginalFilename());
            assertEquals(ProcessStatus.RECEIVED, response.getStatus());
            assertEquals(0, response.getFrameCount());
            assertEquals(0L, response.getArchiveSize());
            assertEquals(TEST_CREATED_AT, response.getCreatedAt());
            assertEquals(TEST_UPDATED_AT, response.getUpdatedAt());

            verify(repository, times(1)).findByKeyEndingWith(searchSuffix);
        }

        @Test
        @DisplayName("Should return status PROCESSING when video is being processed")
        void shouldReturnStatusProcessingWhenVideoIsBeingProcessed() {
            // Arrange
            final String searchSuffix = TEST_KEY.substring(TEST_KEY.lastIndexOf("/") + 1);
            videoDocument.setStatus(ProcessStatus.PROCESSING);
            when(repository.findByKeyEndingWith(searchSuffix)).thenReturn(Optional.of(videoDocument));

            // Act
            final VideoStatusResponse response = videoStatusUseCase.getStatus(TEST_KEY);

            // Assert
            assertNotNull(response);
            assertEquals(ProcessStatus.PROCESSING, response.getStatus());

            verify(repository, times(1)).findByKeyEndingWith(searchSuffix);
        }

        @Test
        @DisplayName("Should return status PROCESSED with frame count and archive size")
        void shouldReturnStatusProcessedWithFrameCountAndArchiveSize() {
            // Arrange
            final String searchSuffix = TEST_KEY.substring(TEST_KEY.lastIndexOf("/") + 1);
            videoDocument.setStatus(ProcessStatus.PROCESSED);
            videoDocument.setFrameCount(120);
            videoDocument.setArchiveSize(512000L);
            when(repository.findByKeyEndingWith(searchSuffix)).thenReturn(Optional.of(videoDocument));

            // Act
            final VideoStatusResponse response = videoStatusUseCase.getStatus(TEST_KEY);

            // Assert
            assertNotNull(response);
            assertEquals(ProcessStatus.PROCESSED, response.getStatus());
            assertEquals(120, response.getFrameCount());
            assertEquals(512000L, response.getArchiveSize());

            verify(repository, times(1)).findByKeyEndingWith(searchSuffix);
        }

        @Test
        @DisplayName("Should return status FAILED when processing failed")
        void shouldReturnStatusFailedWhenProcessingFailed() {
            // Arrange
            final String searchSuffix = TEST_KEY.substring(TEST_KEY.lastIndexOf("/") + 1);
            videoDocument.setStatus(ProcessStatus.FAILED);
            when(repository.findByKeyEndingWith(searchSuffix)).thenReturn(Optional.of(videoDocument));

            // Act
            final VideoStatusResponse response = videoStatusUseCase.getStatus(TEST_KEY);

            // Assert
            assertNotNull(response);
            assertEquals(ProcessStatus.FAILED, response.getStatus());

            verify(repository, times(1)).findByKeyEndingWith(searchSuffix);
        }
    }

    @Nested
    @DisplayName("getStatus() — failure paths")
    class FailurePathTests {

        @Test
        @DisplayName("Should throw VideoNotFoundException when video does not exist")
        void shouldThrowVideoNotFoundExceptionWhenVideoDoesNotExist() {
            // Arrange
            final String searchSuffix = TEST_KEY.substring(TEST_KEY.lastIndexOf("/") + 1);
            when(repository.findByKeyEndingWith(searchSuffix)).thenReturn(Optional.empty());

            // Act / Assert
            final VideoNotFoundException exception = assertThrows(
                VideoNotFoundException.class,
                () -> videoStatusUseCase.getStatus(TEST_KEY)
            );

            assertEquals(TEST_KEY, exception.getVideoKey());
            assertTrue(exception.getMessage().contains(TEST_KEY));

            verify(repository, times(1)).findByKeyEndingWith(searchSuffix);
        }

        @Test
        @DisplayName("Should not call repository with null key")
        void shouldCallRepositoryEvenWithNullKey() {
            // Arrange
            when(repository.findByKeyEndingWith(null)).thenReturn(Optional.empty());

            // Act / Assert
            assertThrows(
                VideoNotFoundException.class,
                () -> videoStatusUseCase.getStatus(null)
            );

            verify(repository, times(1)).findByKeyEndingWith(null);
        }
    }

    @Nested
    @DisplayName("getStatus() — repository interaction")
    class RepositoryInteractionTests {

        @Test
        @DisplayName("Should call findByKeyEndingWith exactly once")
        void shouldCallFindByKeyEndingWithExactlyOnce() {
            // Arrange
            final String searchSuffix = TEST_KEY.substring(TEST_KEY.lastIndexOf("/") + 1);
            videoDocument.setStatus(ProcessStatus.RECEIVED);
            when(repository.findByKeyEndingWith(searchSuffix)).thenReturn(Optional.of(videoDocument));

            // Act
            videoStatusUseCase.getStatus(TEST_KEY);

            // Assert
            verify(repository, times(1)).findByKeyEndingWith(searchSuffix);
            verify(repository, never()).findByProcessedKey(TEST_KEY);
            verify(repository, never()).save(videoDocument);
        }
    }
}
