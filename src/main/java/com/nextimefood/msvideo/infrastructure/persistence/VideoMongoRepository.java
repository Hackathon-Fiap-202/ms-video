package com.nextimefood.msvideo.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VideoMongoRepository extends MongoRepository<VideoDocument, String> {

    Optional<VideoDocument> findByKey(String key);

    Optional<VideoDocument> findByProcessedKey(String processedKey);

    Optional<VideoDocument> findByKeyEndingWith(String suffix);

}
