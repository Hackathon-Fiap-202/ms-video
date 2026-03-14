package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.VideoDownloadResponse;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.application.ports.outgoing.VideoStoragePort;
import com.nextimefood.msvideo.domain.ProcessStatus;
import com.nextimefood.msvideo.domain.exception.VideoNotFoundException;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VideoDownloadUseCase Tests")
class VideoDownloadUseCaseTest {

    @Mock
    private VideoRepositoryPort repository;

    @Mock
    private VideoStoragePort storage;

    @InjectMocks
    private VideoDownloadUseCase videoDownloadUseCase;

    private VideoDocument videoDocument;
    private static final String TEST_PROCESSED_KEY = "abc123.zip";
    private static final String TEST_BUCKET = "test-bucket";
    private static final String TEST_OUTPUT_PREFIX = "video-processed-storage/";
    private static final String EXPECTED_FULL_S3_KEY = TEST_OUTPUT_PREFIX + "end-process/" + TEST_PROCESSED_KEY;
    private static final String TEST_URL = "https://s3.amazonaws.com/test-bucket/video-processed-storage/end-process/abc123.zip?sig=xyz";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(videoDownloadUseCase, "outputPrefix", TEST_OUTPUT_PREFIX);

        videoDocument = new VideoDocument();
        videoDocument.setId("123");
        videoDocument.setKey("video-input-storage/start-process/abc123.mp4");
        videoDocument.setProcessedKey(TEST_PROCESSED_KEY);
        videoDocument.setBucket(TEST_BUCKET);
        videoDocument.setStatus(ProcessStatus.PROCESSED);
        videoDocument.setOriginalFilename("test-video.mp4");
        videoDocument.setContentType("video/mp4");
        videoDocument.setSize(1024L);
    }

    @Test
    @DisplayName("Should generate download URL successfully when video exists")
    void shouldGenerateDownloadUrlSuccessfully() {
        // Arrange
        when(repository.findByProcessedKey(TEST_PROCESSED_KEY)).thenReturn(Optional.of(videoDocument));
        when(storage.generatePresignedUrl(eq(TEST_BUCKET), eq(EXPECTED_FULL_S3_KEY), any(Duration.class)))
                .thenReturn(TEST_URL);

        // Act
        final VideoDownloadResponse response = videoDownloadUseCase.generateDownloadUrl(TEST_PROCESSED_KEY);

        // Assert
        assertNotNull(response);
        assertEquals("123", response.getVideoId());
        assertEquals(TEST_PROCESSED_KEY, response.getKey());
        assertEquals(TEST_URL, response.getDownloadUrl());
        assertEquals("1 hour", response.getExpiresIn());

        verify(repository, times(1)).findByProcessedKey(TEST_PROCESSED_KEY);
        verify(storage, times(1)).generatePresignedUrl(eq(TEST_BUCKET), eq(EXPECTED_FULL_S3_KEY), any(Duration.class));
    }

    @Test
    @DisplayName("Should throw VideoNotFoundException when video does not exist")
    void shouldThrowVideoNotFoundExceptionWhenVideoDoesNotExist() {
        // Arrange
        when(repository.findByProcessedKey(TEST_PROCESSED_KEY)).thenReturn(Optional.empty());

        // Act / Assert
        final VideoNotFoundException exception = assertThrows(
                VideoNotFoundException.class,
                () -> videoDownloadUseCase.generateDownloadUrl(TEST_PROCESSED_KEY)
        );

        assertEquals(TEST_PROCESSED_KEY, exception.getVideoKey());
        assertTrue(exception.getMessage().contains(TEST_PROCESSED_KEY));

        verify(repository, times(1)).findByProcessedKey(TEST_PROCESSED_KEY);
        verify(storage, never()).generatePresignedUrl(anyString(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("Should call storage with correct duration of 1 hour")
    void shouldCallStorageWithCorrectDuration() {
        // Arrange
        when(repository.findByProcessedKey(TEST_PROCESSED_KEY)).thenReturn(Optional.of(videoDocument));
        when(storage.generatePresignedUrl(eq(TEST_BUCKET), eq(EXPECTED_FULL_S3_KEY), any(Duration.class)))
                .thenReturn(TEST_URL);

        // Act
        videoDownloadUseCase.generateDownloadUrl(TEST_PROCESSED_KEY);

        // Assert
        verify(storage).generatePresignedUrl(TEST_BUCKET, EXPECTED_FULL_S3_KEY, Duration.ofHours(1));
    }

    @Test
    @DisplayName("Should reconstruct full S3 key from outputPrefix and processedKey")
    void shouldReconstructFullS3KeyFromPrefixAndProcessedKey() {
        // Arrange
        when(repository.findByProcessedKey(TEST_PROCESSED_KEY)).thenReturn(Optional.of(videoDocument));
        when(storage.generatePresignedUrl(anyString(), anyString(), any(Duration.class))).thenReturn(TEST_URL);

        // Act
        videoDownloadUseCase.generateDownloadUrl(TEST_PROCESSED_KEY);

        // Assert — storage must be called with the full path, not just the filename
        verify(storage).generatePresignedUrl(TEST_BUCKET, EXPECTED_FULL_S3_KEY, Duration.ofHours(1));
    }
}
