package com.nextimefood.msvideo.application.dto;

public class VideoProcessMessage {

    private String bucket;
    private String key;

    public VideoProcessMessage() {
    }

    public VideoProcessMessage(String bucket, String key) {
        this.bucket = bucket;
        this.key = key;
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
}
