package com.nextimefood.msvideo.infrastructure.adapter.storage;

import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3VideoStorageAdapter Tests")
class S3VideoStorageAdapterTest {

    @Mock
    private S3Template s3Template;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3VideoStorageAdapter adapter;

    @Nested
    @DisplayName("upload()")
    class UploadTests {

        @Test
        @DisplayName("Should delegate upload to S3Template")
        void shouldDelegateUploadToS3Template() throws IOException {
            // Arrange
            final String bucket = "test-bucket";
            final String key = "test-key.mp4";
            final InputStream content = new ByteArrayInputStream("video-data".getBytes());

            // Act
            adapter.upload(bucket, key, content);

            // Assert
            verify(s3Template).upload(bucket, key, content);
        }
    }

    @Nested
    @DisplayName("generatePresignedUrl()")
    class GeneratePresignedUrlTests {

        @Test
        @DisplayName("Should return presigned GET URL string from S3Template")
        void shouldReturnPresignedUrlString() throws MalformedURLException {
            // Arrange
            final String bucket = "test-bucket";
            final String key = "test-key.mp4";
            final Duration duration = Duration.ofMinutes(10);
            final URL fakeUrl = URI.create("https://s3.amazonaws.com/test-bucket/test-key.mp4?signed=true").toURL();
            when(s3Template.createSignedGetURL(bucket, key, duration)).thenReturn(fakeUrl);

            // Act
            final String result = adapter.generatePresignedUrl(bucket, key, duration);

            // Assert
            assertEquals(fakeUrl.toString(), result);
        }
    }

    @Nested
    @DisplayName("generatePresignedPutUrl()")
    class GeneratePresignedPutUrlTests {

        @Test
        @DisplayName("Should return presigned PUT URL string from S3Presigner")
        void shouldReturnPresignedPutUrlString() throws MalformedURLException {
            // Arrange
            final String bucket = "test-bucket";
            final String key = "test-key.mp4";
            final Duration duration = Duration.ofMinutes(15);
            final URL fakeUrl = URI.create("https://s3.amazonaws.com/test-bucket/test-key.mp4?X-Amz-Signature=abc").toURL();

            final PresignedPutObjectRequest presignedRequest = org.mockito.Mockito.mock(PresignedPutObjectRequest.class);
            when(presignedRequest.url()).thenReturn(fakeUrl);
            when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedRequest);

            // Act
            final String result = adapter.generatePresignedPutUrl(bucket, key, duration);

            // Assert
            assertEquals(fakeUrl.toString(), result);
            verify(s3Presigner).presignPutObject(any(PutObjectPresignRequest.class));
        }
    }
}
