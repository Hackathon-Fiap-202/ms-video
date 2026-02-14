package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.VideoDownloadResponse;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.application.ports.outgoing.VideoStoragePort;
import com.nextimefood.msvideo.domain.exception.VideoNotFoundException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VideoDownloadUseCase {

    private static final Logger logger = LoggerFactory.getLogger(VideoDownloadUseCase.class);

    private final VideoRepositoryPort repository;
    private final VideoStoragePort storage;

    public VideoDownloadUseCase(VideoRepositoryPort repository, VideoStoragePort storage) {
        this.repository = repository;
        this.storage = storage;
    }

    public VideoDownloadResponse generateDownloadUrl(String key) {
        logger.info("Generating download URL for video key: {}", key);
        
        final var videoOpt = repository.findByKey(key);
        
        if (videoOpt.isEmpty()) {
            logger.warn("Video not found with key: {}", key);
            throw new VideoNotFoundException(key);
        }

        final var video = videoOpt.get();
        final var duration = Duration.ofHours(1);
        final var presignedUrl = storage.generatePresignedUrl(video.getBucket(), video.getKey(), duration);

        logger.info("Successfully generated download URL for video key: {}", key);
        
        return new VideoDownloadResponse(
            video.getId(),
            video.getKey(),
            presignedUrl,
            "1 hour"
        );
    }
}
