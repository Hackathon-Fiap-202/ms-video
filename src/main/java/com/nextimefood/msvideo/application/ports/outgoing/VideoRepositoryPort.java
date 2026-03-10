package com.nextimefood.msvideo.application.ports.outgoing;

import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;

public interface VideoRepositoryPort {

    VideoDocument save(VideoDocument video);

    java.util.Optional<VideoDocument> findByKey(String key);

    java.util.Optional<VideoDocument> findByProcessedKey(String processedKey);

}
