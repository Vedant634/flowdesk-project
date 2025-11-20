package com.flowdesk.flowdesk_backend.controller;

import com.flowdesk.flowdesk_backend.dto.response.NotificationResponse;
import com.flowdesk.flowdesk_backend.service.NotificationService;
import com.flowdesk.flowdesk_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Notification Controller
 * Handles user notifications
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    /**
     * Get all notifications for current user
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications() {
        log.info("Get notifications for current user");
        UUID currentUserId = securityUtils.getCurrentUserId();
        List<NotificationResponse> notifications = notificationService.getUserNotifications(currentUserId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Mark notification as read
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        log.info("Mark notification as read: {}", id);
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Mark all notifications as read
     */
    @PostMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead() {
        log.info("Mark all notifications as read for current user");
        UUID currentUserId = securityUtils.getCurrentUserId();
        notificationService.markAllAsRead(currentUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get unread notification count
     */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        log.info("Get unread count for current user");
        UUID currentUserId = securityUtils.getCurrentUserId();
        long count = notificationService.getUnreadCount(currentUserId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
