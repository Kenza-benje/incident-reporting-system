package com.example.ifraneguard.dto.response;

import com.example.ifraneguard.enums.IncidentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StatusHistoryResponse {
    private Long           id;
    private IncidentStatus fromStatus;
    private String         fromStatusDisplayName;
    private IncidentStatus toStatus;
    private String         toStatusDisplayName;
    private String         changedByName;
    private String         reason;
    private LocalDateTime  changedAt;
    
}