package com.nextimefood.msvideo.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VideoDownloadResponse {

    private String videoId;
    private String key;
    private String downloadUrl;
    private String expiresIn;
}
