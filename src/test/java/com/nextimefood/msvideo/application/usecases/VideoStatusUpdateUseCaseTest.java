package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.VideoStatusEventDTO;
import com.nextimefood.msvideo.application.ports.outgoing.MessagePublisherPort;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.domain.ProcessStatus;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
    private static final String TEST_VIDEO_KEY = "test-video-key";
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
                video.getStatus() == ProcessStatus.PROCESSED &&
                video.getFrameCount() == 100 &&
                video.getArchiveSize() == 5000L
        ));
        verify(publisher, times(1)).publish(TEST_QUEUE, successEvent);
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
    @DisplayName("Should update all fields when processing succeeds")
    void shouldUpdateAllFieldsWhenProcessingSucceeds() {
        when(repository.findByKey(TEST_VIDEO_KEY)).thenReturn(Optional.of(videoDocument));
        when(repository.save(any(VideoDocument.class))).thenReturn(videoDocument);

        videoStatusUpdateUseCase.processVideoStatusUpdate(successEvent);

        verify(repository).save(argThat(video ->
                video.getStatus() == ProcessStatus.PROCESSED &&
                video.getFrameCount() == 100 &&
                video.getArchiveSize() == 5000L
        ));
    }
}
