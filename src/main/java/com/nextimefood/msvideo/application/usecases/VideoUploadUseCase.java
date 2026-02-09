package com.nextimefood.msvideo.application.usecases;

import io.awspring.cloud.s3.S3Template;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Service
public class VideoUploadUseCase {

    private final S3Template s3Template;
    private final SqsTemplate sqsTemplate;

    @Value("${spring.cloud.s3.bucket-name}")
    private String bucketName;
    @Value("${spring.cloud.sqs.queues.video-process-command}")
    private String videoProcessCommandQueue;

    public VideoUploadUseCase(S3Template s3Template, SqsTemplate sqsTemplate) {
        this.s3Template = s3Template;
        this.sqsTemplate = sqsTemplate;
    }

    public String upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }

        final var key = generateUniqueKey(file.getOriginalFilename());

        s3Template.upload(bucketName, "start-process/%s".formatted(key), file.getInputStream());

        sqsTemplate.send(to -> to
                .queue(videoProcessCommandQueue)
                .payload(key)
        );

        return key;
    }

    private String generateUniqueKey(String originalFilename) {git status
        var extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }
}
