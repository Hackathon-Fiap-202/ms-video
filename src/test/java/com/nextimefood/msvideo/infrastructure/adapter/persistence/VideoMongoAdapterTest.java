package com.nextimefood.msvideo.infrastructure.adapter.persistence;

import com.nextimefood.msvideo.domain.ProcessStatus;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import com.nextimefood.msvideo.infrastructure.persistence.VideoMongoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VideoMongoAdapter Tests")
class VideoMongoAdapterTest {

    @Mock
    private VideoMongoRepository repository;

    @InjectMocks
    private VideoMongoAdapter adapter;

    @Nested
    @DisplayName("save()")
    class SaveTests {

        @Test
        @DisplayName("Should save and return video document")
        void shouldSaveAndReturnVideoDocument() {
            // Arrange
            final var doc = new VideoDocument();
            doc.setId("123");
            doc.setStatus(ProcessStatus.RECEIVED);
            when(repository.save(doc)).thenReturn(doc);

            // Act
            final VideoDocument result = adapter.save(doc);

            // Assert
            assertEquals("123", result.getId());
            verify(repository).save(doc);
        }
    }

    @Nested
    @DisplayName("findByKey()")
    class FindByKeyTests {

        @Test
        @DisplayName("Should return present optional when document found")
        void shouldReturnPresentOptionalWhenFound() {
            // Arrange
            final var doc = new VideoDocument();
            doc.setKey("video-key");
            when(repository.findByKey("video-key")).thenReturn(Optional.of(doc));

            // Act
            final Optional<VideoDocument> result = adapter.findByKey("video-key");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("video-key", result.get().getKey());
        }

        @Test
        @DisplayName("Should return empty optional when document not found")
        void shouldReturnEmptyOptionalWhenNotFound() {
            // Arrange
            when(repository.findByKey("missing-key")).thenReturn(Optional.empty());

            // Act
            final Optional<VideoDocument> result = adapter.findByKey("missing-key");

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByProcessedKey()")
    class FindByProcessedKeyTests {

        @Test
        @DisplayName("Should return present optional when document found by processedKey")
        void shouldReturnPresentOptionalWhenFoundByProcessedKey() {
            // Arrange
            final var doc = new VideoDocument();
            doc.setProcessedKey("abc123.zip");
            when(repository.findByProcessedKey("abc123.zip")).thenReturn(Optional.of(doc));

            // Act
            final Optional<VideoDocument> result = adapter.findByProcessedKey("abc123.zip");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("abc123.zip", result.get().getProcessedKey());
        }

        @Test
        @DisplayName("Should return empty optional when document not found by processedKey")
        void shouldReturnEmptyOptionalWhenNotFoundByProcessedKey() {
            // Arrange
            when(repository.findByProcessedKey("missing.zip")).thenReturn(Optional.empty());

            // Act
            final Optional<VideoDocument> result = adapter.findByProcessedKey("missing.zip");

            // Assert
            assertTrue(result.isEmpty());
        }
    }
}
