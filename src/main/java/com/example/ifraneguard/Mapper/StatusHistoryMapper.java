package com.example.ifraneguard.Mapper;


import com.example.ifraneguard.dto.response.StatusHistoryResponse;
import com.example.ifraneguard.Model.StatusHistory;
import org.springframework.stereotype.Component;

/**
 * Converts StatusHistory entities → DTOs for the incident audit trail view.
 */
@Component
public class StatusHistoryMapper {

    public StatusHistoryResponse toResponse(StatusHistory history) {
        if (history == null) return null;

        return StatusHistoryResponse.builder()
                .id(history.getId())
                .fromStatus(history.getFromStatus())
                .fromStatusDisplayName(history.getFromStatus() != null
                        ? history.getFromStatus().getDisplayName() : null)
                .toStatus(history.getToStatus())
                .toStatusDisplayName(history.getToStatus() != null
                        ? history.getToStatus().getDisplayName() : null)
                .changedByName(history.getChangedBy() != null
                        ? history.getChangedBy().getFullName() : null)
                .reason(history.getReason())
                .changedAt(history.getChangedAt())
                .build();
    }
}