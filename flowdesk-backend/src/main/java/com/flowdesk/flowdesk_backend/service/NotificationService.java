package com.flowdesk.flowdesk_backend.service;

import com.flowdesk.flowdesk_backend.dto.response.NotificationResponse;
import com.flowdesk.flowdesk_backend.dto.response.UserResponse;
import com.flowdesk.flowdesk_backend.model.Notification;
import com.flowdesk.flowdesk_backend.model.Task;
import com.flowdesk.flowdesk_backend.model.User;
import com.flowdesk.flowdesk_backend.model.enums.NotificationType;
import com.flowdesk.flowdesk_backend.repository.NotificationRepository;
import com.flowdesk.flowdesk_backend.repository.TaskRepository;
import com.flowdesk.flowdesk_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    /**
     * Create a generic notification
     */
    @Transactional
    public NotificationResponse createNotification(
            UUID userId,
            NotificationType type,
            String title,
            String message,
            UUID taskId) {

        log.info("Creating notification for user: {} of type: {}", userId, type);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setIsRead(false);

        if (taskId != null) {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
            notification.setTask(task);
        }

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created successfully for user: {}", user.getEmail());

        return mapToNotificationResponse(savedNotification);
    }

    /**
     * Get all notifications for a user (ordered by most recent)
     */
    public List<NotificationResponse> getUserNotifications(UUID userId) {
        log.info("Fetching notifications for user: {}", userId);

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notifications for a user
     */
    public List<NotificationResponse> getUnreadNotifications(UUID userId) {
        log.info("Fetching unread notifications for user: {}", userId);

        List<Notification> notifications = notificationRepository.findByUserIdAndIsRead(userId, false);

        return notifications.stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mark a notification as read
     */
    @Transactional
    public void markAsRead(UUID notificationId) {
        log.info("Marking notification as read: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        notification.setIsRead(true);
        notificationRepository.save(notification);

        log.info("Notification marked as read");
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(UUID userId) {
        log.info("Marking all notifications as read for user: {}", userId);

        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsRead(userId, false);

        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);

        log.info("Marked {} notifications as read for user: {}", unreadNotifications.size(), userId);
    }

    /**
     * Get count of unread notifications for a user
     */
    public long getUnreadCount(UUID userId) {
        log.debug("Getting unread count for user: {}", userId);
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    // ============== Helper Methods for Specific Notification Types ==============

    /**
     * Send notification when task is assigned to a developer
     */
    @Transactional
    public void sendTaskAssignedNotification(UUID taskId, UUID assigneeId) {
        log.info("Sending task assigned notification for task: {} to user: {}", taskId, assigneeId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        String title = "New Task Assigned";
        String message = String.format("You have been assigned to task: %s", task.getTitle());

        createNotification(assigneeId, NotificationType.TASK_ASSIGNED, title, message, taskId);
    }

    /**
     * Send notification when developer submits task for review
     */
    @Transactional
    public void sendReviewRequestNotification(UUID taskId, UUID managerId) {
        log.info("Sending review request notification for task: {} to manager: {}", taskId, managerId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        String title = "Task Ready for Review";
        String message = String.format("Task '%s' has been submitted for review by %s %s",
                task.getTitle(),
                task.getAssignedToUser().getFirstName(),
                task.getAssignedToUser().getLastName());

        createNotification(managerId, NotificationType.TASK_ASSIGNED, title, message, taskId);
    }

    /**
     * Send notification when manager approves task
     */
    @Transactional
    public void sendTaskApprovedNotification(UUID taskId, UUID developerId) {
        log.info("Sending task approved notification for task: {} to developer: {}", taskId, developerId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        String title = "Task Approved";
        String message = String.format("Your task '%s' has been approved! Great work!", task.getTitle());

        createNotification(developerId, NotificationType.TASK_APPROVED, title, message, taskId);
    }

    /**
     * Send notification for high-risk tasks or approaching deadlines
     */
    @Transactional
    public void sendRiskAlertNotification(UUID taskId, UUID userId) {
        log.info("Sending risk alert notification for task: {} to user: {}", taskId, userId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        String title = "Task Risk Alert";
        String message = String.format("Task '%s' is flagged as high-risk or approaching deadline. Please review.",
                task.getTitle());

        createNotification(userId, NotificationType.RISK_ALERT, title, message, taskId);
    }

    /**
     * Send notification when deadline is approaching (within 2 days)
     */
    @Transactional
    public void sendDeadlineApproachingNotification(UUID taskId, UUID userId) {
        log.info("Sending deadline approaching notification for task: {} to user: {}", taskId, userId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        String title = "Deadline Approaching";
        String message = String.format("Task '%s' is due on %s. Please complete it soon!",
                task.getTitle(), task.getDueDate());

        createNotification(userId, NotificationType.DEADLINE_APPROACHING, title, message, taskId);
    }

    /**
     * Send notification when manager requests changes on a task
     */
    @Transactional
    public void sendChangesRequestedNotification(UUID taskId, UUID developerId, String comment) {
        log.info("Sending changes requested notification for task: {} to developer: {}", taskId, developerId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        String title = "Changes Requested";
        String message = String.format("Changes requested on task '%s'. Comment: %s",
                task.getTitle(), comment != null ? comment : "Please review feedback");

        createNotification(developerId, NotificationType.CHANGES_REQUESTED, title, message, taskId);
    }

    // Helper methods

    private NotificationResponse mapToNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .taskId(notification.getTask() != null ? notification.getTask().getId() : null)
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
