package com.nextimefood.msvideo.application.dto;

import com.nextimefood.msvideo.domain.ProcessStatus;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VideoStatusEventDTO {

    private String videoKey;
    private boolean success;
    private ProcessStatus status;
    private int frameCount;
    private long archiveSize;
    private String timestamp;

    public VideoStatusEventDTO(String videoKey, boolean success, ProcessStatus status, int frameCount, long archiveSize, String timestamp) {
        this.videoKey = videoKey;
        this.success = success;
        this.status = status;
        this.frameCount = frameCount;
        this.archiveSize = archiveSize;
        this.timestamp = timestamp;
    }

    public VideoStatusEventDTO() {
    }

}
