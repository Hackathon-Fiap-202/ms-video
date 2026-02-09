package com.nextimefood.msvideo.infrastructure.adapter.persistence;

import com.nextimefood.msvideo.application.ports.outgoing.VideoRepositoryPort;
import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import com.nextimefood.msvideo.infrastructure.persistence.VideoMongoRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class VideoMongoAdapter implements VideoRepositoryPort {

    private final VideoMongoRepository repository;

    public VideoMongoAdapter(VideoMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public VideoDocument save(VideoDocument video) {
        if (video.getCreatedAt() == null) {
            video.setCreatedAt(Instant.now());
        }
        video.setUpdatedAt(Instant.now());
        return repository.save(video);
    }
}
