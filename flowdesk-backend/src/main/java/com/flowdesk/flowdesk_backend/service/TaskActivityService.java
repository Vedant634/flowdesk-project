package com.flowdesk.flowdesk_backend.service;

import com.flowdesk.flowdesk_backend.dto.response.TaskActivityResponse;
import com.flowdesk.flowdesk_backend.dto.response.UserResponse;
import com.flowdesk.flowdesk_backend.model.Task;
import com.flowdesk.flowdesk_backend.model.TaskActivity;
import com.flowdesk.flowdesk_backend.model.User;
import com.flowdesk.flowdesk_backend.model.enums.ActivityType;
import com.flowdesk.flowdesk_backend.model.enums.TaskStatus;
import com.flowdesk.flowdesk_backend.repository.TaskActivityRepository;
import com.flowdesk.flowdesk_backend.repository.TaskRepository;
import com.flowdesk.flowdesk_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskActivityService {

    private final TaskActivityRepository taskActivityRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    /**
     * Log a generic task activity
     */
    @Transactional
    public TaskActivityResponse logActivity(
            UUID taskId,
            UUID userId,
            ActivityType type,
            String description) {

        log.info("Logging activity for task: {} by user: {} - Type: {}", taskId, userId, type);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        TaskActivity activity = new TaskActivity();
        activity.setTask(task);
        activity.setUser(user);
        activity.setActivityType(type);
        activity.setDescription(description);

        TaskActivity savedActivity = taskActivityRepository.save(activity);
        log.debug("Activity logged successfully: {}", description);

        return mapToTaskActivityResponse(savedActivity);
    }

    /**
     * Get all activities for a task (ordered by creation time ascending)
     */
    public List<TaskActivityResponse> getTaskActivities(UUID taskId) {
        log.info("Fetching activities for task: {}", taskId);

        // Verify task exists
        taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        List<TaskActivity> activities = taskActivityRepository.findByTaskIdOrderByCreatedAtAsc(taskId);

        return activities.stream()
                .map(this::mapToTaskActivityResponse)
                .collect(Collectors.toList());
    }

    // ============== Helper Methods for Specific Activity Types ==============

    /**
     * Log task creation activity
     */
    @Transactional
    public void logTaskCreated(UUID taskId, UUID userId) {
        log.info("Logging task creation for task: {} by user: {}", taskId, userId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        String description = String.format("%s %s created this task",
                user.getFirstName(), user.getLastName());

        logActivity(taskId, userId, ActivityType.CREATED, description);
    }

    /**
     * Log task assignment activity
     */
    @Transactional
    public void logTaskAssigned(UUID taskId, UUID userId, UUID assignedToId) {
        log.info("Logging task assignment for task: {} to user: {}", taskId, assignedToId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        User assigner = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        User assignedTo = userRepository.findById(assignedToId)
                .orElseThrow(() -> new RuntimeException("Assigned user not found with id: " + assignedToId));

        String description = String.format("%s %s assigned this task to %s %s",
                assigner.getFirstName(), assigner.getLastName(),
                assignedTo.getFirstName(), assignedTo.getLastName());

        logActivity(taskId, userId, ActivityType.ASSIGNED, description);
    }

    /**
     * Log task status change activity
     */
    @Transactional
    public void logStatusChanged(UUID taskId, UUID userId, TaskStatus oldStatus, TaskStatus newStatus) {
        log.info("Logging status change for task: {} from {} to {}", taskId, oldStatus, newStatus);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        String description = String.format("%s %s changed status from %s to %s",
                user.getFirstName(), user.getLastName(),
                formatStatus(oldStatus), formatStatus(newStatus));

        logActivity(taskId, userId, ActivityType.STATUS_CHANGED, description);
    }

    /**
     * Log task completion activity
     */
    @Transactional
    public void logTaskCompleted(UUID taskId, UUID userId) {
        log.info("Logging task completion for task: {} by user: {}", taskId, userId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        String description = String.format("%s %s completed this task",
                user.getFirstName(), user.getLastName());

        logActivity(taskId, userId, ActivityType.COMPLETED, description);
    }

    // Helper methods

    private String formatStatus(TaskStatus status) {
        return Pattern.compile("\\b(\\w)")
                .matcher(status.name().replace("_", " ").toLowerCase())
                .replaceAll(m -> m.group().toUpperCase());
    }

    private TaskActivityResponse mapToTaskActivityResponse(TaskActivity activity) {
        return TaskActivityResponse.builder()
                .id(activity.getId())
                .taskId(activity.getTask().getId())
                .user(mapToUserResponse(activity.getUser()))
                .activityType(activity.getActivityType())
                .description(activity.getDescription())
                .createdAt(activity.getCreatedAt())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}
