package com.nextimefood.msvideo.infrastructure.controller;

import com.nextimefood.msvideo.application.dto.VideoDownloadResponse;
import com.nextimefood.msvideo.application.usecases.VideoDownloadUseCase;
import com.nextimefood.msvideo.application.usecases.VideoUploadUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
            when(file.getOriginalFilename()).thenReturn("video.mp4");
            when(videoUploadUseCase.upload(file, userId)).thenReturn(expectedKey);

            // Act
            final String result = controller.handleVideoUpload(file, userId);

            // Assert
            assertEquals(expectedKey, result);
            verify(videoUploadUseCase).upload(file, userId);
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
}
