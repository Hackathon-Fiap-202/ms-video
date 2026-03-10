package com.nextimefood.msvideo.domain;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Video {

    private String id;
    private String bucket;
    private String key;
    private String originalFilename;
    private String contentType;
    private long size;
    private ProcessStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private int frameCount;
    private long archiveSize;

    public Video() {
    }

    public Video(String id, String bucket, String key, String originalFilename, String contentType,
                 long size, ProcessStatus status, Instant createdAt, Instant updatedAt,
                 int frameCount, long archiveSize) {
        this.id = id;
        this.bucket = bucket;
        this.key = key;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.frameCount = frameCount;
        this.archiveSize = archiveSize;
    }

}
