package com.example.ifraneguard.service;

import com.example.ifraneguard.Mapper.NotificationMapper;
import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.Model.Notification;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.dto.response.NotificationResponse;
import com.example.ifraneguard.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles creating and retrieving in-app notifications.
 *
 * The authority dashboard frontend reads unread notifications to display
 * the success/error banners after actions like assignment.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper     notificationMapper;

    /**
     * Send a success notification to a specific user about an incident.
     * Example: "The incident report has been assigned successfully."
     */
    @Transactional
    public void sendSuccess(User recipient, Incident incident, String message) {
        save(recipient, incident, message, "SUCCESS");
    }

    /**
     * Send an error notification.
     * Example: "The incident report failed to be assigned."
     */
    @Transactional
    public void sendError(User recipient, Incident incident, String message) {
        save(recipient, incident, message, "ERROR");
    }

    @Transactional
    public void sendInfo(User recipient, Incident incident, String message) {
        save(recipient, incident, message, "INFO");
    }

    @Transactional
    public void sendWarning(User recipient, Incident incident, String message) {
        save(recipient, incident, message, "WARNING");
    }

    /** All unread notifications for the logged-in user (for the dashboard badge). */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(User user) {
        return notificationRepository.findByRecipientAndIsReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    public long countUnread(User user) {
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

    /** Mark all as read when user opens the notification panel. */
    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadForUser(user);
    }

    private void save(User recipient, Incident incident, String message, String type) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .incident(incident)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        log.debug("Notification [{}] sent to user {}: {}", type, recipient.getEmail(), message);
    }
}