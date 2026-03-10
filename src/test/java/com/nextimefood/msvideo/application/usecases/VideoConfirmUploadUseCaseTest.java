package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.ports.outgoing.MessagePublisherPort;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.domain.ProcessStatus;
import com.nextimefood.msvideo.domain.exception.VideoNotFoundException;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VideoConfirmUploadUseCase Tests")
class VideoConfirmUploadUseCaseTest {

    @Mock
    private VideoRepositoryPort repository;

    @Mock
    private MessagePublisherPort publisher;

    @InjectMocks
    private VideoConfirmUploadUseCase useCase;

    private static final String QUEUE = "video-process-command";
    private static final String KEY = "video-input-storage/start-process/abc.mp4";
    private static final String BUCKET = "test-bucket";

    @Nested
    @DisplayName("Successful confirmation")
    class SuccessPathTests {

        @Test
        @DisplayName("Should publish message and update status to PROCESSING")
        void shouldPublishMessageAndUpdateStatus() {
            // Arrange
            ReflectionTestUtils.setField(useCase, "videoProcessCommandQueue", QUEUE);
            final var doc = new VideoDocument();
            doc.setBucket(BUCKET);
            doc.setKey(KEY);
            doc.setStatus(ProcessStatus.RECEIVED);
            when(repository.findByKey(KEY)).thenReturn(Optional.of(doc));
            when(repository.save(any(VideoDocument.class))).thenReturn(doc);

            // Act
            useCase.confirm(KEY);

            // Assert
            verify(publisher).publish(eq(QUEUE), any());
            final ArgumentCaptor<VideoDocument> captor = ArgumentCaptor.forClass(VideoDocument.class);
            verify(repository).save(captor.capture());
            assertEquals(ProcessStatus.PROCESSING, captor.getValue().getStatus());
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorPathTests {

        @Test
        @DisplayName("Should throw VideoNotFoundException when key does not exist")
        void shouldThrowVideoNotFoundExceptionWhenKeyDoesNotExist() {
            // Arrange
            ReflectionTestUtils.setField(useCase, "videoProcessCommandQueue", QUEUE);
            when(repository.findByKey(KEY)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(VideoNotFoundException.class, () -> useCase.confirm(KEY));
        }
    }
}
