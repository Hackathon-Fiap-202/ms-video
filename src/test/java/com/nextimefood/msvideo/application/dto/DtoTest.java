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
            assertNull(msg.getRecords());
        }

        @Test
        @DisplayName("Should create with all-args constructor and expose structured fields")
        void shouldCreateWithAllArgsConstructor() {
            // Act
            final var msg = new VideoProcessMessage("my-bucket", "video/key.mp4");

            // Assert
            assertEquals(1, msg.getRecords().size());
            assertEquals("my-bucket", msg.getRecords().get(0).getS3().getBucket().getName());
            assertEquals("video/key.mp4", msg.getRecords().get(0).getS3().getObject().getKey());
        }

        @Test
        @DisplayName("Should set fields via setters")
        void shouldSetFieldsViaSetters() {
            // Arrange
            final var msg = new VideoProcessMessage();

            // Act
            msg.setRecords(java.util.List.of(new VideoProcessMessage.Record(
                    new VideoProcessMessage.S3(
                            new VideoProcessMessage.Bucket("bucket-2"),
                            new VideoProcessMessage.ObjectInfo("key-2.mp4")
                    )
            )));

            // Assert
            assertEquals("bucket-2", msg.getRecords().get(0).getS3().getBucket().getName());
            assertEquals("key-2.mp4", msg.getRecords().get(0).getS3().getObject().getKey());
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
    @DisplayName("VideoUploadPresignRequest")
    class VideoUploadPresignRequestTests {

        @Test
        @DisplayName("Should create with no-arg constructor")
        void shouldCreateWithNoArgConstructor() {
            // Act
            final var req = new VideoUploadPresignRequest();

            // Assert
            assertNull(req.getFilename());
            assertNull(req.getContentType());
        }

        @Test
        @DisplayName("Should create with all-args constructor and expose fields")
        void shouldCreateWithAllArgsConstructor() {
            // Act
            final var req = new VideoUploadPresignRequest("video.mp4", "video/mp4");

            // Assert
            assertEquals("video.mp4", req.getFilename());
            assertEquals("video/mp4", req.getContentType());
        }

        @Test
        @DisplayName("Should set fields via setters")
        void shouldSetFieldsViaSetters() {
            // Arrange
            final var req = new VideoUploadPresignRequest();

            // Act
            req.setFilename("clip.mp4");
            req.setContentType("video/avi");

            // Assert
            assertEquals("clip.mp4", req.getFilename());
            assertEquals("video/avi", req.getContentType());
        }
    }

    @Nested
    @DisplayName("VideoUploadPresignResponse")
    class VideoUploadPresignResponseTests {

        @Test
        @DisplayName("Should create with no-arg constructor")
        void shouldCreateWithNoArgConstructor() {
            // Act
            final var resp = new VideoUploadPresignResponse();

            // Assert
            assertNull(resp.getKey());
            assertNull(resp.getUploadUrl());
            assertNull(resp.getExpiresIn());
        }

        @Test
        @DisplayName("Should create with all-args constructor and expose fields")
        void shouldCreateWithAllArgsConstructor() {
            // Act
            final var resp = new VideoUploadPresignResponse("some-key", "https://s3.example.com/put-url", "15 minutes");

            // Assert
            assertEquals("some-key", resp.getKey());
            assertEquals("https://s3.example.com/put-url", resp.getUploadUrl());
            assertEquals("15 minutes", resp.getExpiresIn());
        }

        @Test
        @DisplayName("Should set fields via setters")
        void shouldSetFieldsViaSetters() {
            // Arrange
            final var resp = new VideoUploadPresignResponse();

            // Act
            resp.setKey("other-key");
            resp.setUploadUrl("https://s3.example.com/other");
            resp.setExpiresIn("10 minutes");

            // Assert
            assertEquals("other-key", resp.getKey());
            assertEquals("https://s3.example.com/other", resp.getUploadUrl());
            assertEquals("10 minutes", resp.getExpiresIn());
        }
    }
}

