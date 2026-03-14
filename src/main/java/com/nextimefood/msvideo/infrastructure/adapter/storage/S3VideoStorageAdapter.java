package com.nextimefood.msvideo.infrastructure.adapter.storage;

import com.nextimefood.msvideo.application.ports.outgoing.VideoStoragePort;
import io.awspring.cloud.s3.S3Template;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@Primary
public class S3VideoStorageAdapter implements VideoStoragePort {

    private final S3Template s3Template;
    private final S3Presigner s3Presigner;

    public S3VideoStorageAdapter(S3Template s3Template, S3Presigner s3Presigner) {
        this.s3Template = s3Template;
        this.s3Presigner = s3Presigner;
    }

    @Override
    public void upload(String bucket, String key, InputStream content) throws IOException {
        s3Template.upload(bucket, key, content);
    }

    @Override
    public String generatePresignedUrl(String bucket, String key, Duration duration) {
        return s3Template.createSignedGetURL(bucket, key, duration).toString();
    }

    @Override
    public String generatePresignedPutUrl(String bucket, String key, Duration duration) {
        final var presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(duration)
                .putObjectRequest(req -> req.bucket(bucket).key(key))
                .build();
        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }
}
