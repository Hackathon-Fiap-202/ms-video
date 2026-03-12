package com.nextimefood.msvideo.infrastructure.controller;

import com.nextimefood.msvideo.application.dto.VideoDownloadResponse;
import com.nextimefood.msvideo.application.dto.VideoItemResponseDTO;
import com.nextimefood.msvideo.application.dto.VideoPageResponseDTO;
import com.nextimefood.msvideo.application.dto.VideoStatusResponse;
import com.nextimefood.msvideo.application.dto.VideoUploadPresignRequest;
import com.nextimefood.msvideo.application.dto.VideoUploadPresignResponse;
import com.nextimefood.msvideo.application.usecases.ListVideosUseCase;
import com.nextimefood.msvideo.application.usecases.VideoConfirmUploadUseCase;
import com.nextimefood.msvideo.application.usecases.VideoDownloadUseCase;
import com.nextimefood.msvideo.application.usecases.VideoStatusUseCase;
import com.nextimefood.msvideo.application.usecases.VideoUploadPresignUseCase;
import com.nextimefood.msvideo.application.usecases.VideoUploadUseCase;
import com.nextimefood.msvideo.domain.ProcessStatus;
import com.nextimefood.msvideo.domain.exception.VideoNotFoundException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VideoController Tests")
class VideoControllerTest {

    @Mock
    private VideoUploadUseCase videoUploadUseCase;

    @Mock
    private VideoDownloadUseCase videoDownloadUseCase;

    @Mock
    private VideoUploadPresignUseCase videoUploadPresignUseCase;

    @Mock
    private VideoConfirmUploadUseCase videoConfirmUploadUseCase;

    @Mock
    private VideoStatusUseCase videoStatusUseCase;

    @Mock
    private ListVideosUseCase listVideosUseCase;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private VideoController controller;

    @Nested
    @DisplayName("handleVideoUpload()")
    class UploadTests {

        @Test
        @DisplayName("Should delegate upload and return key")
        void shouldDelegateUploadAndReturnKey() {
            // Arrange
            final String userId = "user-123";
            final String expectedKey = "video-input-storage/start-process/abc.mp4";
            when(videoUploadUseCase.upload(file, userId)).thenReturn(expectedKey);

            // Act
            final String result = controller.handleVideoUpload(file, userId);

            // Assert
            assertEquals(expectedKey, result);
            verify(videoUploadUseCase).upload(file, userId);
        }
    }

    @Nested
    @DisplayName("presignUpload()")
    class PresignUploadTests {

        @Test
        @DisplayName("Should return 200 with presign response")
        void shouldReturn200WithPresignResponse() {
            // Arrange
            final String userId = "user-456";
            final var request = new VideoUploadPresignRequest("video.mp4", "video/mp4");
            final var presignResponse = new VideoUploadPresignResponse("some-key", "https://s3.example.com/put-url", "15 minutes");
            when(videoUploadPresignUseCase.presign(request, userId)).thenReturn(presignResponse);

            // Act
            final ResponseEntity<VideoUploadPresignResponse> result = controller.presignUpload(request, userId);

            // Assert
            assertNotNull(result);
            assertEquals(200, result.getStatusCode().value());
            assertEquals(presignResponse, result.getBody());
            verify(videoUploadPresignUseCase).presign(request, userId);
        }
    }

    @Nested
    @DisplayName("confirmUpload()")
    class ConfirmUploadTests {

        @Test
        @DisplayName("Should delegate confirmation to use case")
        void shouldDelegateConfirmationToUseCase() {
            // Arrange
            final String key = "video-input-storage/start-process/abc.mp4";

            // Act
            controller.confirmUpload(key);

            // Assert
            verify(videoConfirmUploadUseCase).confirm(key);
        }
    }

    @Nested
    @DisplayName("getDownloadUrl()")
    class DownloadTests {

        @Test
        @DisplayName("Should return 200 with download response")
        void shouldReturn200WithDownloadResponse() {
            // Arrange
            final String key = "video-key-abc";
            final var response = new VideoDownloadResponse("id-1", key, "https://s3.example.com/signed", "10m");
            when(videoDownloadUseCase.generateDownloadUrl(key)).thenReturn(response);

            // Act
            final ResponseEntity<VideoDownloadResponse> result = controller.getDownloadUrl(key);

            // Assert
            assertNotNull(result);
            assertEquals(200, result.getStatusCode().value());
            assertEquals(response, result.getBody());
            verify(videoDownloadUseCase).generateDownloadUrl(key);
        }
    }

    @Nested
    @DisplayName("getVideoStatus()")
    class VideoStatusTests {

        @Test
        @DisplayName("Should return 200 with status response when video exists")
        void shouldReturn200WithStatusResponseWhenVideoExists() {
            // Arrange
            final String key = "video-input-storage/start-process/abc123.mp4";
            final var statusResponse = new VideoStatusResponse(
                "doc-id-001",
                key,
                "my-video.mp4",
                ProcessStatus.PROCESSING,
                0,
                0L,
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T10:05:00Z")
            );
            when(videoStatusUseCase.getStatus(key)).thenReturn(statusResponse);

            // Act
            final ResponseEntity<VideoStatusResponse> result = controller.getVideoStatus(key);

            // Assert
            assertNotNull(result);
            assertEquals(200, result.getStatusCode().value());
            assertNotNull(result.getBody());
            assertEquals("doc-id-001", result.getBody().getVideoId());
            assertEquals(key, result.getBody().getKey());
            assertEquals(ProcessStatus.PROCESSING, result.getBody().getStatus());
            verify(videoStatusUseCase).getStatus(key);
        }

        @Test
        @DisplayName("Should return 200 with PROCESSED status and frame metadata")
        void shouldReturn200WithProcessedStatusAndFrameMetadata() {
            // Arrange
            final String key = "video-input-storage/start-process/xyz.mp4";
            final var statusResponse = new VideoStatusResponse(
                "doc-id-002",
                key,
                "clip.mp4",
                ProcessStatus.PROCESSED,
                300,
                1024000L,
                Instant.parse("2026-01-01T08:00:00Z"),
                Instant.parse("2026-01-01T08:30:00Z")
            );
            when(videoStatusUseCase.getStatus(key)).thenReturn(statusResponse);

            // Act
            final ResponseEntity<VideoStatusResponse> result = controller.getVideoStatus(key);

            // Assert
            assertNotNull(result);
            assertEquals(200, result.getStatusCode().value());
            assertNotNull(result.getBody());
            assertEquals(ProcessStatus.PROCESSED, result.getBody().getStatus());
            assertEquals(300, result.getBody().getFrameCount());
            assertEquals(1024000L, result.getBody().getArchiveSize());
            verify(videoStatusUseCase).getStatus(key);
        }

        @Test
        @DisplayName("Should propagate VideoNotFoundException when video is not found")
        void shouldPropagateVideoNotFoundExceptionWhenVideoIsNotFound() {
            // Arrange
            final String key = "non-existent-key";
            when(videoStatusUseCase.getStatus(key)).thenThrow(new VideoNotFoundException(key));

            // Act / Assert
            assertThrows(
                VideoNotFoundException.class,
                () -> controller.getVideoStatus(key)
            );

            verify(videoStatusUseCase).getStatus(key);
        }
    }

    @Nested
    @DisplayName("listVideos()")
    class ListVideosTests {

        @Test
        @DisplayName("Should return 200 with paginated body")
        void shouldReturn200WithPaginatedBody() {
            // Arrange
            final var item = new VideoItemResponseDTO();
            item.setId("doc-id-001");
            final var response = new VideoPageResponseDTO(List.of(item), 0, 1, 1, 5);
            when(listVideosUseCase.execute(0, 5)).thenReturn(response);

            // Act
            final ResponseEntity<VideoPageResponseDTO> result = controller.listVideos(0, 5);

            // Assert
            assertNotNull(result);
            assertEquals(200, result.getStatusCode().value());
            assertEquals(response, result.getBody());
            verify(listVideosUseCase).execute(0, 5);
        }

        @Test
        @DisplayName("Should use default page=0 and size=5 when not provided")
        void shouldUseDefaultPageAndSize() throws Exception {
            // Arrange
            final var response = new VideoPageResponseDTO(List.of(), 0, 0, 0, 5);
            when(listVideosUseCase.execute(0, 5)).thenReturn(response);
            MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

            // Act & Assert
            mockMvc.perform(get("/videos"))
                    .andExpect(status().isOk());

            verify(listVideosUseCase).execute(0, 5);
        }

        @Test
        @DisplayName("Should return empty content with 200 when no videos exist")
        void shouldReturnEmptyContentWith200WhenNoVideosExist() throws Exception {
            // Arrange
            final var response = new VideoPageResponseDTO(List.of(), 0, 0, 0, 5);
            when(listVideosUseCase.execute(0, 5)).thenReturn(response);
            MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

            // Act & Assert
            mockMvc.perform(get("/videos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
            
            verify(listVideosUseCase).execute(0, 5);
        }
    }
}

