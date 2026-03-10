package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.VideoDownloadResponse;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.application.ports.outgoing.VideoStoragePort;
import com.nextimefood.msvideo.domain.ProcessStatus;
import com.nextimefood.msvideo.domain.exception.VideoNotFoundException;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    private static final String TEST_KEY = "test-video-key";
    private static final String TEST_BUCKET = "test-bucket";
    private static final String TEST_URL = "https://s3.amazonaws.com/test-bucket/test-video-key?signature=xyz";

    @BeforeEach
    void setUp() {
        videoDocument = new VideoDocument();
        videoDocument.setId("123");
        videoDocument.setKey(TEST_KEY);
        videoDocument.setBucket(TEST_BUCKET);
        videoDocument.setStatus(ProcessStatus.PROCESSED);
        videoDocument.setOriginalFilename("test-video.mp4");
        videoDocument.setContentType("video/mp4");
        videoDocument.setSize(1024L);
    }

    @Test
    @DisplayName("Should generate download URL successfully when video exists")
    void shouldGenerateDownloadUrlSuccessfully() {
        when(repository.findByKey(TEST_KEY)).thenReturn(Optional.of(videoDocument));
        when(storage.generatePresignedUrl(eq(TEST_BUCKET), eq(TEST_KEY), any(Duration.class)))
                .thenReturn(TEST_URL);

        VideoDownloadResponse response = videoDownloadUseCase.generateDownloadUrl(TEST_KEY);

        assertNotNull(response);
        assertEquals("123", response.getVideoId());
        assertEquals(TEST_KEY, response.getKey());
        assertEquals(TEST_URL, response.getDownloadUrl());
        assertEquals("1 hour", response.getExpiresIn());

        verify(repository, times(1)).findByKey(TEST_KEY);
        verify(storage, times(1)).generatePresignedUrl(eq(TEST_BUCKET), eq(TEST_KEY), any(Duration.class));
    }

    @Test
    @DisplayName("Should throw VideoNotFoundException when video does not exist")
    void shouldThrowVideoNotFoundExceptionWhenVideoDoesNotExist() {
        when(repository.findByKey(TEST_KEY)).thenReturn(Optional.empty());

        VideoNotFoundException exception = assertThrows(
                VideoNotFoundException.class,
                () -> videoDownloadUseCase.generateDownloadUrl(TEST_KEY)
        );

        assertEquals(TEST_KEY, exception.getVideoKey());
        assertTrue(exception.getMessage().contains(TEST_KEY));

        verify(repository, times(1)).findByKey(TEST_KEY);
        verify(storage, never()).generatePresignedUrl(anyString(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("Should call storage with correct duration")
    void shouldCallStorageWithCorrectDuration() {
        when(repository.findByKey(TEST_KEY)).thenReturn(Optional.of(videoDocument));
        when(storage.generatePresignedUrl(eq(TEST_BUCKET), eq(TEST_KEY), any(Duration.class)))
                .thenReturn(TEST_URL);

        videoDownloadUseCase.generateDownloadUrl(TEST_KEY);

        verify(storage).generatePresignedUrl(TEST_BUCKET, TEST_KEY, Duration.ofHours(1));
    }
}
