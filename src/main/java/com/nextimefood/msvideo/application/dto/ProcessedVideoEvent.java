package com.nextimefood.msvideo.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedVideoEvent {
    private String cognito_user_id;
    private String key_name;
    private String status;
}
