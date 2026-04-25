package com.example.ifraneguard.repository;

import com.example.ifraneguard.Model.Notification;
import com.example.ifraneguard.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** All unread notifications for a user — shown in dashboard notification area. */
    List<Notification> findByRecipientAndIsReadFalseOrderByCreatedAtDesc(User recipient);

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    long countByRecipientAndIsReadFalse(User recipient);

    /** Mark all notifications as read for a user in one query (bulk update). */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :recipient AND n.isRead = false")
    void markAllAsReadForUser(@Param("recipient") User recipient);
}