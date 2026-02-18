package com.nextimefood.msvideo.application.dto;

public class VideoUploadRequest {

    private String originalFilename;
    private String contentType;
    private long size;

    public VideoUploadRequest() {
    }

    public VideoUploadRequest(String originalFilename, String contentType, long size) {
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
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
}
