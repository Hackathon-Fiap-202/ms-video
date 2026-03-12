package com.nextimefood.msvideo.infrastructure.controller;

import com.nextimefood.msvideo.application.dto.VideoDownloadResponse;
import com.nextimefood.msvideo.application.dto.VideoStatusResponse;
import com.nextimefood.msvideo.application.dto.VideoUploadPresignRequest;
import com.nextimefood.msvideo.application.dto.VideoUploadPresignResponse;
import com.nextimefood.msvideo.application.dto.VideoPageResponseDTO;
import com.nextimefood.msvideo.application.usecases.ListVideosUseCase;
import com.nextimefood.msvideo.application.usecases.VideoConfirmUploadUseCase;
import com.nextimefood.msvideo.application.usecases.VideoDownloadUseCase;
import com.nextimefood.msvideo.application.usecases.VideoStatusUseCase;
import com.nextimefood.msvideo.application.usecases.VideoUploadPresignUseCase;
import com.nextimefood.msvideo.application.usecases.VideoUploadUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/videos")
public class VideoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoController.class);

    private final VideoUploadUseCase videoUploadUseCase;
    private final VideoDownloadUseCase videoDownloadUseCase;
    private final VideoUploadPresignUseCase videoUploadPresignUseCase;
    private final VideoConfirmUploadUseCase videoConfirmUploadUseCase;
    private final VideoStatusUseCase videoStatusUseCase;
    private final ListVideosUseCase listVideosUseCase;

    public VideoController(
            VideoUploadUseCase videoUploadUseCase,
            VideoDownloadUseCase videoDownloadUseCase,
            VideoUploadPresignUseCase videoUploadPresignUseCase,
            VideoConfirmUploadUseCase videoConfirmUploadUseCase,
            VideoStatusUseCase videoStatusUseCase,
            ListVideosUseCase listVideosUseCase) {
        this.videoUploadUseCase = videoUploadUseCase;
        this.videoDownloadUseCase = videoDownloadUseCase;
        this.videoUploadPresignUseCase = videoUploadPresignUseCase;
        this.videoConfirmUploadUseCase = videoConfirmUploadUseCase;
        this.videoStatusUseCase = videoStatusUseCase;
        this.listVideosUseCase = listVideosUseCase;
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String handleVideoUpload(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-Cognito-User-Id") String userId) {
        LOGGER.info("Received upload request");
        return videoUploadUseCase.upload(file, userId);
    }

    @PostMapping("/upload/presign")
    public ResponseEntity<VideoUploadPresignResponse> presignUpload(
            @RequestBody VideoUploadPresignRequest request,
            @RequestHeader("X-Cognito-User-Id") String userId) {
        LOGGER.info("Received presign upload request");
        final var response = videoUploadPresignUseCase.presign(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm/{key}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void confirmUpload(@PathVariable String key) {
        LOGGER.info("Received upload confirmation");
        videoConfirmUploadUseCase.confirm(key);
    }

    @GetMapping("/download/{key}")
    public ResponseEntity<VideoDownloadResponse> getDownloadUrl(@PathVariable String key) {
        LOGGER.info("Received download request");
        final var response = videoDownloadUseCase.generateDownloadUrl(key);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{key}/status")
    public ResponseEntity<VideoStatusResponse> getVideoStatus(@PathVariable String key) {
        LOGGER.info("Received status request");
        final var response = videoStatusUseCase.getStatus(key);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<VideoPageResponseDTO> listVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        LOGGER.info("Received list videos request for page {} with size {}", page, size);
        final var response = listVideosUseCase.execute(page, size);
        return ResponseEntity.ok(response);
    }
}
