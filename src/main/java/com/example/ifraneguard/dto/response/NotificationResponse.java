package com.example.ifraneguard.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long          id;
    private String        message;
    private String        type;        // "SUCCESS" | "ERROR" | "INFO" | "WARNING"
    @JsonProperty("isRead")
    private boolean       isRead;
    private Long          incidentId;  // null for system-level notifications
    private LocalDateTime createdAt;
}