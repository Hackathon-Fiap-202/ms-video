package com.nextimefood.msvideo.infrastructure.controller;

import com.nextimefood.msvideo.application.usecases.VideoUploadUseCase;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/videos")
public class VideoController {

    VideoUploadUseCase videoUploadUseCase;

    public VideoController(VideoUploadUseCase videoUploadUseCase) {
        this.videoUploadUseCase = videoUploadUseCase;
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String handleVideoUpload(@RequestParam("file") MultipartFile file) throws IOException {
        return videoUploadUseCase.upload(file);
    }
}
