package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.VideoDownloadResponse;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.application.ports.outgoing.VideoStoragePort;
import com.nextimefood.msvideo.domain.exception.VideoNotFoundException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VideoDownloadUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoDownloadUseCase.class);
    private static final String OUTPUT_SUBPREFIX = "end-process/";
    private static final Duration DOWNLOAD_DURATION = Duration.ofHours(1);
    private static final String EXPIRES_IN_LABEL = "1 hour";

    private final VideoRepositoryPort repository;
    private final VideoStoragePort storage;

    @Value("${spring.cloud.s3.output-prefix}")
    private String outputPrefix;

    public VideoDownloadUseCase(VideoRepositoryPort repository, VideoStoragePort storage) {
        this.repository = repository;
        this.storage = storage;
    }

    public VideoDownloadResponse generateDownloadUrl(String processedKey) {
        LOGGER.info("Generating download URL and finding by processed key");

        final var videoOpt = repository.findByProcessedKey(processedKey);

        if (videoOpt.isEmpty()) {
            LOGGER.warn("Video not found with processed key");
            throw new VideoNotFoundException(processedKey);
        }

        final var video = videoOpt.get();
        final var fullS3Key = outputPrefix + OUTPUT_SUBPREFIX + video.getProcessedKey();
        final var presignedUrl = storage.generatePresignedUrl(video.getBucket(), fullS3Key, DOWNLOAD_DURATION);

        LOGGER.info("Successfully generated download URL");

        return new VideoDownloadResponse(
            video.getId(),
            video.getProcessedKey(),
            presignedUrl,
            EXPIRES_IN_LABEL
        );
    }
}
