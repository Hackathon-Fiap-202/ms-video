package com.nextimefood.msvideo.application.dto;

import com.nextimefood.msvideo.domain.ProcessStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoItemResponseDTO {
    private String id;
    private String originalFilename;
    private ProcessStatus status;
    private int frameCount;
    private long archiveSize;
    private String processedKey;
    private Instant createdAt;
    private Instant updatedAt;
}
