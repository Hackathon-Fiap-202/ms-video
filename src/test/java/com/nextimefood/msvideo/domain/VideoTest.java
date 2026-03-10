package com.nextimefood.msvideo.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Video Domain Tests")
class VideoTest {

    @Test
    @DisplayName("Should create Video with no-arg constructor and set fields via setters")
    void shouldCreateVideoWithNoArgConstructorAndSetters() {
        // Arrange
        final var now = Instant.now();
        final var video = new Video();

        // Act
        video.setId("vid-1");
        video.setBucket("bucket");
        video.setKey("key.mp4");
        video.setOriginalFilename("original.mp4");
        video.setContentType("video/mp4");
        video.setSize(1024L);
        video.setStatus(ProcessStatus.PROCESSING);
        video.setCreatedAt(now);
        video.setUpdatedAt(now);
        video.setFrameCount(30);
        video.setArchiveSize(256L);

        // Assert
        assertEquals("vid-1", video.getId());
        assertEquals("bucket", video.getBucket());
        assertEquals("key.mp4", video.getKey());
        assertEquals("original.mp4", video.getOriginalFilename());
        assertEquals("video/mp4", video.getContentType());
        assertEquals(1024L, video.getSize());
        assertEquals(ProcessStatus.PROCESSING, video.getStatus());
        assertEquals(now, video.getCreatedAt());
        assertEquals(now, video.getUpdatedAt());
        assertEquals(30, video.getFrameCount());
        assertEquals(256L, video.getArchiveSize());
    }

    @Test
    @DisplayName("Should create Video using all-args constructor")
    void shouldCreateVideoUsingAllArgsConstructor() {
        // Arrange
        final var now = Instant.now();

        // Act
        final var video = new Video("vid-2", "bucket-2", "key-2.mp4", "file.mp4",
                "video/mp4", 2048L, ProcessStatus.PROCESSED, now, now, 60, 512L);

        // Assert
        assertEquals("vid-2", video.getId());
        assertEquals("bucket-2", video.getBucket());
        assertEquals("key-2.mp4", video.getKey());
        assertEquals("file.mp4", video.getOriginalFilename());
        assertEquals("video/mp4", video.getContentType());
        assertEquals(2048L, video.getSize());
        assertEquals(ProcessStatus.PROCESSED, video.getStatus());
        assertEquals(now, video.getCreatedAt());
        assertEquals(now, video.getUpdatedAt());
        assertEquals(60, video.getFrameCount());
        assertEquals(512L, video.getArchiveSize());
    }

    @Test
    @DisplayName("Should have null fields when using no-arg constructor")
    void shouldHaveNullFieldsWithNoArgConstructor() {
        // Arrange / Act
        final var video = new Video();

        // Assert
        assertNull(video.getId());
        assertNull(video.getBucket());
        assertNull(video.getKey());
        assertNull(video.getStatus());
    }
}
