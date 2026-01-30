package com.nextimefood.msvideo.configuration;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class SqsConfiguration {

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Bean
    @Profile({"local", "test"})
    public SqsAsyncClient sqsAsyncClientLocal(
            @Value("${spring.cloud.aws.sqs.endpoint}") String endpoint
    ) {
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("test", "test")
                        )
                )
                .build();
    }

    @Bean
    @Profile("!local & !test")
    public SqsAsyncClient sqsAsyncClientEks() {
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .build();
    }
}
