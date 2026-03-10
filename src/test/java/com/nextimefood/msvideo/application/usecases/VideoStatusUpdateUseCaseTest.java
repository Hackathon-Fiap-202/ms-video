package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.ProcessedVideoEvent;
import com.nextimefood.msvideo.application.dto.VideoStatusEventDTO;
import com.nextimefood.msvideo.application.ports.outgoing.MessagePublisherPort;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.domain.ProcessStatus;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VideoStatusUpdateUseCase Tests")
class VideoStatusUpdateUseCaseTest {

    @Mock
    private VideoRepositoryPort repository;

    @Mock
    private MessagePublisherPort publisher;

    @InjectMocks
    private VideoStatusUpdateUseCase videoStatusUpdateUseCase;

    private VideoDocument videoDocument;
    private VideoStatusEventDTO successEvent;
    private VideoStatusEventDTO failedEvent;
    private static final String TEST_VIDEO_KEY = "video-input-storage/start-process/abc123.mp4";
    private static final String EXPECTED_PROCESSED_KEY = "abc123.zip";
    private static final String TEST_QUEUE = "test-queue";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(videoStatusUpdateUseCase, "videoProcessedEventQueue", TEST_QUEUE);

        videoDocument = new VideoDocument();
        videoDocument.setId("123");
        videoDocument.setKey(TEST_VIDEO_KEY);
        videoDocument.setStatus(ProcessStatus.PROCESSING);

        successEvent = new VideoStatusEventDTO();
        successEvent.setVideoKey(TEST_VIDEO_KEY);
        successEvent.setSuccess(true);
        successEvent.setStatus(ProcessStatus.PROCESSED);
        successEvent.setFrameCount(100);
        successEvent.setArchiveSize(5000L);

        failedEvent = new VideoStatusEventDTO();
        failedEvent.setVideoKey(TEST_VIDEO_KEY);
        failedEvent.setSuccess(false);
        failedEvent.setStatus(ProcessStatus.FAILED);
        failedEvent.setFrameCount(0);
        failedEvent.setArchiveSize(0L);
    }

    @Test
    @DisplayName("Should update video successfully when processing succeeds")
    void shouldUpdateVideoSuccessfullyWhenProcessingSucceeds() {
        when(repository.findByKey(TEST_VIDEO_KEY)).thenReturn(Optional.of(videoDocument));
        when(repository.save(any(VideoDocument.class))).thenReturn(videoDocument);

        videoStatusUpdateUseCase.processVideoStatusUpdate(successEvent);

        verify(repository, times(1)).findByKey(TEST_VIDEO_KEY);
        verify(repository, times(1)).save(argThat(video ->
                video.getStatus() == ProcessStatus.PROCESSED
                && video.getFrameCount() == 100
                && video.getArchiveSize() == 5000L
                && EXPECTED_PROCESSED_KEY.equals(video.getProcessedKey())
        ));
        verify(publisher, times(1)).publish(
                eq(TEST_QUEUE),
                argThat(arg -> arg instanceof ProcessedVideoEvent evt
                        && TEST_VIDEO_KEY.equals(evt.getKeyName())
                        && "PROCESSED".equals(evt.getStatus()))
        );
    }

    @Test
    @DisplayName("Should update video status only when processing fails")
    void shouldUpdateVideoStatusOnlyWhenProcessingFails() {
        when(repository.findByKey(TEST_VIDEO_KEY)).thenReturn(Optional.of(videoDocument));
        when(repository.save(any(VideoDocument.class))).thenReturn(videoDocument);

        videoStatusUpdateUseCase.processVideoStatusUpdate(failedEvent);

        verify(repository, times(1)).findByKey(TEST_VIDEO_KEY);
        verify(repository, times(1)).save(argThat(video ->
                video.getStatus() == ProcessStatus.FAILED
        ));
        verify(publisher, never()).publish(anyString(), any());
    }

    @Test
    @DisplayName("Should not update video when video is not found")
    void shouldNotUpdateVideoWhenVideoIsNotFound() {
        when(repository.findByKey(TEST_VIDEO_KEY)).thenReturn(Optional.empty());

        videoStatusUpdateUseCase.processVideoStatusUpdate(successEvent);

        verify(repository, times(1)).findByKey(TEST_VIDEO_KEY);
        verify(repository, never()).save(any(VideoDocument.class));
        verify(publisher, never()).publish(anyString(), any());
    }

    @Test
    @DisplayName("Should not publish message when processing fails")
    void shouldNotPublishMessageWhenProcessingFails() {
        when(repository.findByKey(TEST_VIDEO_KEY)).thenReturn(Optional.of(videoDocument));
        when(repository.save(any(VideoDocument.class))).thenReturn(videoDocument);

        videoStatusUpdateUseCase.processVideoStatusUpdate(failedEvent);

        verify(publisher, never()).publish(anyString(), any());
    }

    @Test
    @DisplayName("Should update all fields including processedKey when processing succeeds")
    void shouldUpdateAllFieldsWhenProcessingSucceeds() {
        when(repository.findByKey(TEST_VIDEO_KEY)).thenReturn(Optional.of(videoDocument));
        when(repository.save(any(VideoDocument.class))).thenReturn(videoDocument);

        videoStatusUpdateUseCase.processVideoStatusUpdate(successEvent);

        verify(repository).save(argThat(video ->
                video.getStatus() == ProcessStatus.PROCESSED
                && video.getFrameCount() == 100
                && video.getArchiveSize() == 5000L
                && EXPECTED_PROCESSED_KEY.equals(video.getProcessedKey())
        ));
    }

    @Test
    @DisplayName("Should not set processedKey when processing fails")
    void shouldNotSetProcessedKeyWhenProcessingFails() {
        when(repository.findByKey(TEST_VIDEO_KEY)).thenReturn(Optional.of(videoDocument));
        when(repository.save(any(VideoDocument.class))).thenReturn(videoDocument);

        videoStatusUpdateUseCase.processVideoStatusUpdate(failedEvent);

        verify(repository).save(argThat(video ->
                video.getStatus() == ProcessStatus.FAILED
                && video.getProcessedKey() == null
        ));
    }

    @Test
    @DisplayName("Should derive processedKey from key with no extension")
    void shouldDeriveProcessedKeyFromKeyWithNoExtension() {
        // Arrange
        final var eventNoExt = new VideoStatusEventDTO();
        eventNoExt.setVideoKey("video-input-storage/start-process/abc123");
        eventNoExt.setSuccess(true);
        eventNoExt.setStatus(ProcessStatus.PROCESSED);
        eventNoExt.setFrameCount(10);
        eventNoExt.setArchiveSize(1000L);

        when(repository.findByKey("video-input-storage/start-process/abc123")).thenReturn(Optional.of(videoDocument));
        when(repository.save(any(VideoDocument.class))).thenReturn(videoDocument);

        videoStatusUpdateUseCase.processVideoStatusUpdate(eventNoExt);

        verify(repository).save(argThat(video ->
                "abc123.zip".equals(video.getProcessedKey())
        ));
    }
}
