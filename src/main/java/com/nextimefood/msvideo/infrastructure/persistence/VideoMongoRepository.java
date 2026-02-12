package com.nextimefood.msvideo.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface VideoMongoRepository extends MongoRepository<VideoDocument, String> {

    java.util.Optional<VideoDocument> findByKey(String key);

}
