package com.example.ifraneguard.Mapper;

import com.example.ifraneguard.dto.response.NotificationResponse;
import com.example.ifraneguard.entity.Notification;
import org.springframework.stereotype.Component;

/**
 * Converts Notification entities → DTOs for the dashboard notification panel.
 */
@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) return null;

        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.isRead())
                .incidentId(notification.getIncident() != null
                        ? notification.getIncident().getId() : null)
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
