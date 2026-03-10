package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.VideoUploadPresignRequest;
import com.nextimefood.msvideo.application.dto.VideoUploadPresignResponse;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.application.ports.outgoing.VideoStoragePort;
import com.nextimefood.msvideo.domain.ProcessStatus;
import com.nextimefood.msvideo.domain.exception.InvalidFileException;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VideoUploadPresignUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoUploadPresignUseCase.class);
    private static final Duration PRESIGN_DURATION = Duration.ofMinutes(15);
    private static final String EXPIRES_IN_LABEL = "15 minutes";

    private final VideoStoragePort storage;
    private final VideoRepositoryPort repository;

    @Value("${spring.cloud.s3.bucket-name}")
    private String bucketName;

    @Value("${spring.cloud.s3.input-prefix:video-input-storage/}")
    private String inputPrefix;

    public VideoUploadPresignUseCase(VideoStoragePort storage, VideoRepositoryPort repository) {
        this.storage = storage;
        this.repository = repository;
    }

    public VideoUploadPresignResponse presign(VideoUploadPresignRequest request, String userId) {
        LOGGER.info("Generating presigned upload URL for file: {} user: {}", request.getFilename(), userId);

        validateRequest(request);

        final var key = generateUniqueKey(request.getFilename());
        final var presignedUrl = storage.generatePresignedPutUrl(bucketName, key, PRESIGN_DURATION);

        saveReceived(request, key, userId);

        LOGGER.info("Presigned upload URL generated for key: {}", key);
        return new VideoUploadPresignResponse(key, presignedUrl, EXPIRES_IN_LABEL);
    }

    private void validateRequest(VideoUploadPresignRequest request) {
        if (request == null || request.getFilename() == null || request.getFilename().isBlank()) {
            LOGGER.warn("Presign request has invalid or missing filename");
            throw new InvalidFileException("Nome do arquivo inválido");
        }
    }

    private void saveReceived(VideoUploadPresignRequest request, String key, String userId) {
        LOGGER.debug("Saving video document with status RECEIVED for key: {}", key);
        final var doc = new VideoDocument();
        doc.setBucket(bucketName);
        doc.setKey(key);
        doc.setOriginalFilename(request.getFilename());
        doc.setContentType(request.getContentType());
        doc.setCognitoUserId(userId);
        doc.setStatus(ProcessStatus.RECEIVED);
        repository.save(doc);
    }

    private String generateUniqueKey(String filename) {
        var extension = "";
        if (filename != null && filename.contains(".")) {
            extension = filename.substring(filename.lastIndexOf("."));
        }
        return inputPrefix + "start-process/%s%s".formatted(UUID.randomUUID(), extension);
    }
}
