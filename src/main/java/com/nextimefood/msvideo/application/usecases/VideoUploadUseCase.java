package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.VideoProcessMessage;
import com.nextimefood.msvideo.application.dto.VideoUploadRequest;
import com.nextimefood.msvideo.application.mapper.VideoRequestMapper;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.domain.VideoStatus;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import com.nextimefood.msvideo.application.ports.outgoing.MessagePublisherPort;
import com.nextimefood.msvideo.application.ports.outgoing.VideoStoragePort;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoUploadUseCase {

    private final VideoStoragePort storage;
    private final MessagePublisherPort publisher;
    private final VideoRepositoryPort repository;
    private final VideoRequestMapper mapper;

    @Value("${spring.cloud.s3.bucket-name}")
    private String bucketName;
    @Value("${spring.cloud.sqs.queues.video-process-command}")
    private String videoProcessCommandQueue;

    public VideoUploadUseCase(VideoStoragePort storage, MessagePublisherPort publisher, VideoRepositoryPort repository, VideoRequestMapper mapper) {
        this.storage = storage;
        this.publisher = publisher;
        this.repository = repository;
        this.mapper = mapper;
    }

    public String upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }
        final var key = generateUniqueKey(file.getOriginalFilename());
        var doc = saveReceived(file, key);
        uploadFile(file, key);
        publishMessage(key);
        updateStatus(doc, VideoStatus.PROCESSING);
        return key;
    }

    private VideoDocument saveReceived(MultipartFile file, String key) {
        var request = new VideoUploadRequest(file.getOriginalFilename(), file.getContentType(), file.getSize());
        VideoDocument doc = mapper.toDocument(request);
        doc.setBucket(bucketName);
        doc.setKey(key);
        doc.setStatus(VideoStatus.RECEIVED);
        return repository.save(doc);
    }

    private void uploadFile(MultipartFile file, String key) throws IOException {
        storage.upload(bucketName, key, file.getInputStream());
    }

    private void publishMessage(String key) {
        var payload = new VideoProcessMessage(bucketName, key);
        publisher.publish(videoProcessCommandQueue, payload);
    }

    private void updateStatus(VideoDocument doc, VideoStatus status) {
        doc.setStatus(status);
        repository.save(doc);
    }

    private String generateUniqueKey(String originalFilename) {
        var extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "start-process/%s%s".formatted(UUID.randomUUID(), extension);
    }
}
