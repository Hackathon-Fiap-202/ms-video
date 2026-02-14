package com.nextimefood.msvideo.infrastructure.adapter.storage;

import com.nextimefood.msvideo.application.ports.outgoing.VideoStoragePort;
import io.awspring.cloud.s3.S3Template;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class S3VideoStorageAdapter implements VideoStoragePort {

    private final S3Template s3Template;

    public S3VideoStorageAdapter(S3Template s3Template) {
        this.s3Template = s3Template;
    }

    @Override
    public void upload(String bucket, String key, InputStream content) throws IOException {
        s3Template.upload(bucket, key, content);
    }

    @Override
    public String generatePresignedUrl(String bucket, String key, Duration duration) {
        return s3Template.createSignedGetURL(bucket, key, duration).toString();
    }
}
