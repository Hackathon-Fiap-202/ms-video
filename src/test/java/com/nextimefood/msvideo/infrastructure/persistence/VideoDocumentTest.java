package com.nextimefood.msvideo.infrastructure.persistence;

import com.nextimefood.msvideo.domain.ProcessStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("VideoDocument Tests")
class VideoDocumentTest {

    @Test
    @DisplayName("Should have RECEIVED as default status")
    void shouldHaveReceivedAsDefaultStatus() {
        // Arrange / Act
        final var doc = new VideoDocument();

        // Assert
        assertEquals(ProcessStatus.RECEIVED, doc.getStatus());
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void shouldSetAndGetAllFields() {
        // Arrange
        final var doc = new VideoDocument();
        final var now = Instant.now();

        // Act
        doc.setId("id-1");
        doc.setBucket("my-bucket");
        doc.setKey("some/key.mp4");
        doc.setOriginalFilename("video.mp4");
        doc.setContentType("video/mp4");
        doc.setSize(2048L);
        doc.setCognitoUserId("user-abc");
        doc.setStatus(ProcessStatus.PROCESSING);
        doc.setCreatedAt(now);
        doc.setUpdatedAt(now);
        doc.setFrameCount(60);
        doc.setArchiveSize(512L);

        // Assert
        assertEquals("id-1", doc.getId());
        assertEquals("my-bucket", doc.getBucket());
        assertEquals("some/key.mp4", doc.getKey());
        assertEquals("video.mp4", doc.getOriginalFilename());
        assertEquals("video/mp4", doc.getContentType());
        assertEquals(2048L, doc.getSize());
        assertEquals("user-abc", doc.getCognitoUserId());
        assertEquals(ProcessStatus.PROCESSING, doc.getStatus());
        assertEquals(now, doc.getCreatedAt());
        assertEquals(now, doc.getUpdatedAt());
        assertEquals(60, doc.getFrameCount());
        assertEquals(512L, doc.getArchiveSize());
    }

    @Test
    @DisplayName("Should have null fields by default except status")
    void shouldHaveNullFieldsByDefault() {
        // Arrange / Act
        final var doc = new VideoDocument();

        // Assert
        assertNull(doc.getId());
        assertNull(doc.getBucket());
        assertNull(doc.getKey());
        assertNull(doc.getOriginalFilename());
        assertNull(doc.getContentType());
        assertNull(doc.getCognitoUserId());
        assertNull(doc.getCreatedAt());
        assertNull(doc.getUpdatedAt());
    }
}
