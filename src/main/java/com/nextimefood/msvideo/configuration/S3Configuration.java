package com.nextimefood.msvideo.configuration;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Configuration {

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Bean
    @Profile({"local", "test"})
    public S3Client s3ClientLocal(
            @Value("${spring.cloud.s3.endpoint}") String endpoint
    ) {
        return S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .forcePathStyle(true) // Necessário para o LocalStack
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("test", "test")
                        )
                )
                .build();
    }

    @Bean
    @Profile("!local & !test")
    public S3Client s3ClientProd() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build();
    }
}

