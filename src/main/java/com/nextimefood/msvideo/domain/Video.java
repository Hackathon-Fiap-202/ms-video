package com.nextimefood.msvideo.domain;

import java.time.Instant;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }

    public long getArchiveSize() {
        return archiveSize;
    }

    public void setArchiveSize(long archiveSize) {
        this.archiveSize = archiveSize;
    }
}
