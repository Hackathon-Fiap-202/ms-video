package com.nextimefood.msvideo.infrastructure.controller;

import com.nextimefood.msvideo.application.dto.VideoDownloadResponse;
import com.nextimefood.msvideo.application.usecases.VideoDownloadUseCase;
import com.nextimefood.msvideo.application.usecases.VideoUploadUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/videos")
public class VideoController {

    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);

    private final VideoUploadUseCase videoUploadUseCase;
    private final VideoDownloadUseCase videoDownloadUseCase;

    public VideoController(VideoUploadUseCase videoUploadUseCase, VideoDownloadUseCase videoDownloadUseCase) {
        this.videoUploadUseCase = videoUploadUseCase;
        this.videoDownloadUseCase = videoDownloadUseCase;
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String handleVideoUpload(@RequestParam("file") MultipartFile file) {
        logger.info("Received upload request for file: {}", file.getOriginalFilename());
        return videoUploadUseCase.upload(file);
    }

    @GetMapping("/download/{key}")
    public ResponseEntity<VideoDownloadResponse> getDownloadUrl(@PathVariable String key) {
        logger.info("Received download request for video key: {}", key);
        final var response = videoDownloadUseCase.generateDownloadUrl(key);
        return ResponseEntity.ok(response);
    }
}
