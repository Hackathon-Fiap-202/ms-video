package com.nextimefood.msvideo.application.ports.outgoing;

import com.nextimefood.msvideo.infrastructure.persistence.VideoDocument;

public interface VideoRepositoryPort {

    VideoDocument save(VideoDocument video);

}
