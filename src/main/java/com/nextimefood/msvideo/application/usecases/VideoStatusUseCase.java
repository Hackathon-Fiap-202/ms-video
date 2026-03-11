package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.VideoStatusResponse;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.domain.exception.VideoNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VideoStatusUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoStatusUseCase.class);

    private final VideoRepositoryPort repository;

    public VideoStatusUseCase(VideoRepositoryPort repository) {
        this.repository = repository;
    }

    public VideoStatusResponse getStatus(String key) {
        LOGGER.info("Fetching status for video key: {}", key);

        final var videoOpt = repository.findByKey(key);

        if (videoOpt.isEmpty()) {
            LOGGER.warn("Video not found with key: {}", key);
            throw new VideoNotFoundException(key);
        }

        final var video = videoOpt.get();

        LOGGER.info("Found video with key: {} and status: {}", key, video.getStatus());

        return new VideoStatusResponse(
            video.getId(),
            video.getKey(),
            video.getOriginalFilename(),
            video.getStatus(),
            video.getFrameCount(),
            video.getArchiveSize(),
            video.getCreatedAt(),
            video.getUpdatedAt()
        );
    }
}
