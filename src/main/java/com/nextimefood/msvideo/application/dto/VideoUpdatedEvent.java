package com.nextimefood.msvideo.application.dto;

import com.nextimefood.msvideo.domain.VideoStatus;

/**
 * DTO representing the video updated event message
 */
public class VideoUpdatedEvent {

    private String videoId;
    private VideoStatus status;
    private String message;

    public VideoUpdatedEvent() {
    }

    public VideoUpdatedEvent(String videoId, VideoStatus status, String message) {
        this.videoId = videoId;
        this.status = status;
        this.message = message;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public VideoStatus getStatus() {
        return status;
    }

    public void setStatus(VideoStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "VideoUpdatedEvent{" +
                "videoId='" + videoId + '\'' +
                ", status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}
