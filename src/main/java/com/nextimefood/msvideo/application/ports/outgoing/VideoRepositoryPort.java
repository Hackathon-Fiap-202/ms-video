package com.nextimefood.msvideo.application.ports.outgoing;

import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VideoRepositoryPort {

    VideoDocument save(VideoDocument video);

    java.util.Optional<VideoDocument> findByKey(String key);

    java.util.Optional<VideoDocument> findByProcessedKey(String processedKey);

    java.util.Optional<VideoDocument> findByKeyEndingWith(String suffix);

    Page<VideoDocument> findAll(Pageable pageable);

}
