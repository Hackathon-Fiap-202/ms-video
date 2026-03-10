package com.nextimefood.msvideo.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedVideoEvent {
    @JsonProperty("cognito_user_id")
    private String cognitoUserId;
    @JsonProperty("key_name")
    private String keyName;
    private String status;
}
