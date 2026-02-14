package com.nextimefood.msvideo.application.usecases;

import com.nextimefood.msvideo.application.dto.VideoProcessMessage;
import com.nextimefood.msvideo.application.dto.VideoUploadRequest;
import com.nextimefood.msvideo.application.mapper.VideoRequestMapper;
import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.domain.ProcessStatus;
import com.nextimefood.msvideo.domain.exception.InvalidFileException;
import com.nextimefood.msvideo.domain.exception.VideoUploadException;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import com.nextimefood.msvideo.application.ports.outgoing.MessagePublisherPort;
import com.nextimefood.msvideo.application.ports.outgoing.VideoStoragePort;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoUploadUseCase {

    private static final Logger logger = LoggerFactory.getLogger(VideoUploadUseCase.class);

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

    public String upload(MultipartFile file) {
        logger.info("Starting video upload process for file: {}", file.getOriginalFilename());
        
        validateFile(file);
        
        try {
            final var key = generateUniqueKey(file.getOriginalFilename());
            logger.debug("Generated unique key for video: {}", key);
            
            var doc = saveReceived(file, key);
            uploadFile(file, key);
            publishMessage(key);
            updateStatus(doc, ProcessStatus.PROCESSING);
            
            logger.info("Video upload completed successfully with key: {}", key);
            return key;
        } catch (IOException e) {
            logger.error("Failed to upload video file: {}", file.getOriginalFilename(), e);
            throw new VideoUploadException("Erro ao fazer upload do vídeo", e);
        } catch (Exception e) {
            logger.error("Unexpected error during video upload: {}", file.getOriginalFilename(), e);
            throw new VideoUploadException("Erro inesperado ao processar upload do vídeo", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.warn("Attempted to upload empty file");
            throw new InvalidFileException("Arquivo vazio");
        }
        
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            logger.warn("Attempted to upload file without name");
            throw new InvalidFileException("Nome do arquivo inválido");
        }
    }

    private VideoDocument saveReceived(MultipartFile file, String key) {
        logger.debug("Saving video document with status RECEIVED for key: {}", key);
        var request = new VideoUploadRequest(file.getOriginalFilename(), file.getContentType(), file.getSize());
        VideoDocument doc = mapper.toDocument(request);
        doc.setBucket(bucketName);
        doc.setKey(key);
        doc.setStatus(ProcessStatus.RECEIVED);
        return repository.save(doc);
    }

    private void uploadFile(MultipartFile file, String key) throws IOException {
        logger.debug("Uploading file to S3 bucket: {} with key: {}", bucketName, key);
        storage.upload(bucketName, key, file.getInputStream());
        logger.debug("File uploaded successfully to S3");
    }

    private void publishMessage(String key) {
        logger.debug("Publishing message to video process command queue for key: {}", key);
        var payload = new VideoProcessMessage(bucketName, key);
        publisher.publish(videoProcessCommandQueue, payload);
        logger.debug("Message published successfully");
    }

    private void updateStatus(VideoDocument doc, ProcessStatus status) {
        logger.debug("Updating video status to: {} for key: {}", status, doc.getKey());
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
