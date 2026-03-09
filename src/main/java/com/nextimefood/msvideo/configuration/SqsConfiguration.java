package com.nextimefood.msvideo.configuration;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.QueueNotFoundStrategy;
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
            @Value("${spring.cloud.sqs.endpoint}") String endpoint
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

    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(SqsAsyncClient sqsAsyncClient) {
        return SqsMessageListenerContainerFactory
                .builder()
                .sqsAsyncClient(sqsAsyncClient)
                .configure(options -> options
                        .queueNotFoundStrategy(QueueNotFoundStrategy.CREATE))
                .build();
    }

}
