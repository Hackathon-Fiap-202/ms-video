package com.nextimefood.msvideo.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.nextimefood.msvideo.infrastructure.persistence")
@EnableMongoAuditing
public class MongoConfiguration {
}
