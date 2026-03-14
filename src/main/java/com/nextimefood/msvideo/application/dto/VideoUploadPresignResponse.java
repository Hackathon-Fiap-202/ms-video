package com.nextimefood.msvideo.application.dto;

public class VideoUploadPresignResponse {

    private String key;
    private String uploadUrl;
    private String expiresIn;

    public VideoUploadPresignResponse() {
    }

    public VideoUploadPresignResponse(String key, String uploadUrl, String expiresIn) {
        this.key = key;
        this.uploadUrl = uploadUrl;
        this.expiresIn = expiresIn;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }
}
