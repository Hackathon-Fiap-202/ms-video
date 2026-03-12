package com.nextimefood.msvideo.infrastructure.adapter.persistence;

import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import com.nextimefood.msvideo.infrastructure.persistence.VideoMongoRepository;
import org.springframework.stereotype.Component;

@Component
public class VideoMongoAdapter implements VideoRepositoryPort {

    private final VideoMongoRepository repository;

    public VideoMongoAdapter(VideoMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public VideoDocument save(VideoDocument video) {
        return repository.save(video);
    }

    @Override
    public java.util.Optional<VideoDocument> findByKey(String key) {
        return repository.findByKey(key);
    }

    @Override
    public java.util.Optional<VideoDocument> findByProcessedKey(String processedKey) {
        return repository.findByProcessedKey(processedKey);
    }

    @Override
    public java.util.Optional<VideoDocument> findByKeyEndingWith(String suffix) {
        return repository.findByKeyEndingWith(suffix);
    }

    @Override
    public org.springframework.data.domain.Page<VideoDocument> findAllByCognitoUserId(String cognitoUserId, org.springframework.data.domain.Pageable pageable) {
        return repository.findAllByCognitoUserId(cognitoUserId, pageable);
    }
}
