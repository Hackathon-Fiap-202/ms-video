package com.nextimefood.msvideo.application.dto;

import com.nextimefood.msvideo.domain.ProcessStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VideoStatusResponse {

    private String videoId;
    private String key;
    private String originalFilename;
    private ProcessStatus status;
    private int frameCount;
    private long archiveSize;
    private Instant createdAt;
    private Instant updatedAt;
}
