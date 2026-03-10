package com.nextimefood.msvideo.application.ports.outgoing;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

public interface VideoStoragePort {

    void upload(String bucket, String key, InputStream content) throws IOException;

    String generatePresignedUrl(String bucket, String key, Duration duration);

}
