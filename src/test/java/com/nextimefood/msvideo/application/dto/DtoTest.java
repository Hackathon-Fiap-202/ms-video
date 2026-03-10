package com.nextimefood.msvideo.application.dto;

import com.nextimefood.msvideo.domain.ProcessStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DTO Tests")
class DtoTest {

    @Nested
    @DisplayName("VideoProcessMessage")
    class VideoProcessMessageTests {

        @Test
        @DisplayName("Should create with no-arg constructor")
        void shouldCreateWithNoArgConstructor() {
            // Act
            final var msg = new VideoProcessMessage();

            // Assert
            assertNull(msg.getBucket());
            assertNull(msg.getKey());
        }

        @Test
        @DisplayName("Should create with all-args constructor and expose fields via getters")
        void shouldCreateWithAllArgsConstructor() {
            // Act
            final var msg = new VideoProcessMessage("my-bucket", "video/key.mp4");

            // Assert
            assertEquals("my-bucket", msg.getBucket());
            assertEquals("video/key.mp4", msg.getKey());
        }

        @Test
        @DisplayName("Should set fields via setters")
        void shouldSetFieldsViaSetters() {
            // Arrange
            final var msg = new VideoProcessMessage();

            // Act
            msg.setBucket("bucket-2");
            msg.setKey("key-2.mp4");

            // Assert
            assertEquals("bucket-2", msg.getBucket());
            assertEquals("key-2.mp4", msg.getKey());
        }
    }

    @Nested
    @DisplayName("VideoUploadRequest")
    class VideoUploadRequestTests {

        @Test
        @DisplayName("Should create with no-arg constructor")
        void shouldCreateWithNoArgConstructor() {
            // Act
            final var req = new VideoUploadRequest();

            // Assert
            assertNull(req.getOriginalFilename());
            assertNull(req.getContentType());
            assertEquals(0L, req.getSize());
        }

        @Test
        @DisplayName("Should create with all-args constructor and expose fields")
        void shouldCreateWithAllArgsConstructor() {
            // Act
            final var req = new VideoUploadRequest("video.mp4", "video/mp4", 1024L);

            // Assert
            assertEquals("video.mp4", req.getOriginalFilename());
            assertEquals("video/mp4", req.getContentType());
            assertEquals(1024L, req.getSize());
        }

        @Test
        @DisplayName("Should set fields via setters")
        void shouldSetFieldsViaSetters() {
            // Arrange
            final var req = new VideoUploadRequest();

            // Act
            req.setOriginalFilename("new.mp4");
            req.setContentType("video/avi");
            req.setSize(2048L);

            // Assert
            assertEquals("new.mp4", req.getOriginalFilename());
            assertEquals("video/avi", req.getContentType());
            assertEquals(2048L, req.getSize());
        }
    }

    @Nested
    @DisplayName("VideoStatusEventDTO")
    class VideoStatusEventDTOTests {

        @Test
        @DisplayName("Should create with no-arg constructor")
        void shouldCreateWithNoArgConstructor() {
            // Act
            final var dto = new VideoStatusEventDTO();

            // Assert
            assertNull(dto.getVideoKey());
            assertNull(dto.getStatus());
        }

        @Test
        @DisplayName("Should create with all-args constructor and expose fields")
        void shouldCreateWithAllArgsConstructor() {
            // Act
            final var dto = new VideoStatusEventDTO("key-1", true, ProcessStatus.PROCESSED, 30, 1024L, "2026-01-01T00:00:00Z");

            // Assert
            assertEquals("key-1", dto.getVideoKey());
            assertTrue(dto.isSuccess());
            assertEquals(ProcessStatus.PROCESSED, dto.getStatus());
            assertEquals(30, dto.getFrameCount());
            assertEquals(1024L, dto.getArchiveSize());
            assertEquals("2026-01-01T00:00:00Z", dto.getTimestamp());
        }

        @Test
        @DisplayName("Should set fields via setters")
        void shouldSetFieldsViaSetters() {
            // Arrange
            final var dto = new VideoStatusEventDTO();

            // Act
            dto.setVideoKey("key-2");
            dto.setSuccess(false);
            dto.setStatus(ProcessStatus.FAILED);
            dto.setFrameCount(60);
            dto.setArchiveSize(2048L);
            dto.setTimestamp("2026-06-01T00:00:00Z");

            // Assert
            assertEquals("key-2", dto.getVideoKey());
            assertEquals(ProcessStatus.FAILED, dto.getStatus());
            assertEquals(60, dto.getFrameCount());
            assertEquals(2048L, dto.getArchiveSize());
            assertEquals("2026-06-01T00:00:00Z", dto.getTimestamp());
        }
    }

    @Nested
    @DisplayName("ProcessedVideoEvent")
    class ProcessedVideoEventTests {

        @Test
        @DisplayName("Should build with builder and expose fields")
        void shouldBuildWithBuilder() {
            // Act
            final var event = ProcessedVideoEvent.builder()
                    .cognitoUserId("user-123")
                    .keyName("video/key.mp4")
                    .status("DONE")
                    .build();

            // Assert
            assertEquals("user-123", event.getCognitoUserId());
            assertEquals("video/key.mp4", event.getKeyName());
            assertEquals("DONE", event.getStatus());
        }

        @Test
        @DisplayName("Should create with no-arg constructor")
        void shouldCreateWithNoArgConstructor() {
            // Act
            final var event = new ProcessedVideoEvent();

            // Assert
            assertNull(event.getCognitoUserId());
            assertNull(event.getKeyName());
            assertNull(event.getStatus());
        }

        @Test
        @DisplayName("Should create with all-args constructor")
        void shouldCreateWithAllArgsConstructor() {
            // Act
            final var event = new ProcessedVideoEvent("user-1", "key.mp4", "PROCESSING");

            // Assert
            assertEquals("user-1", event.getCognitoUserId());
            assertEquals("key.mp4", event.getKeyName());
            assertEquals("PROCESSING", event.getStatus());
        }
    }

    @Nested
    @DisplayName("VideoDownloadResponse")
    class VideoDownloadResponseTests {

        @Test
        @DisplayName("Should create with all-args constructor and expose fields")
        void shouldCreateWithAllArgsConstructor() {
            // Act
            final var resp = new VideoDownloadResponse("id-1", "key.mp4", "https://s3.example.com/signed", "10m");

            // Assert
            assertEquals("id-1", resp.getVideoId());
            assertEquals("key.mp4", resp.getKey());
            assertEquals("https://s3.example.com/signed", resp.getDownloadUrl());
            assertEquals("10m", resp.getExpiresIn());
        }

        @Test
        @DisplayName("Should set fields via setters")
        void shouldSetFieldsViaSetters() {
            // Arrange
            final var resp = new VideoDownloadResponse();

            // Act
            resp.setVideoId("id-2");
            resp.setKey("other.mp4");
            resp.setDownloadUrl("https://s3.example.com/other");
            resp.setExpiresIn("5m");

            // Assert
            assertEquals("id-2", resp.getVideoId());
            assertEquals("other.mp4", resp.getKey());
            assertEquals("https://s3.example.com/other", resp.getDownloadUrl());
            assertEquals("5m", resp.getExpiresIn());
        }
    }
}
