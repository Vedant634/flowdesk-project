package com.flowdesk.flowdesk_backend.service;

import com.flowdesk.flowdesk_backend.dto.request.CreateSubtaskRequest;
import com.flowdesk.flowdesk_backend.dto.response.SubtaskResponse;
import com.flowdesk.flowdesk_backend.model.Subtask;
import com.flowdesk.flowdesk_backend.model.Task;
import com.flowdesk.flowdesk_backend.repository.SubtaskRepository;
import com.flowdesk.flowdesk_backend.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubtaskService {

    private final SubtaskRepository subtaskRepository;
    private final TaskRepository taskRepository;

    /**
     * Create a new subtask
     */
    @Transactional
    public SubtaskResponse createSubtask(CreateSubtaskRequest request) {
        log.info("Creating subtask: {} for task: {}", request.getTitle(), request.getTaskId());

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + request.getTaskId()));

        Subtask subtask = new Subtask();
        subtask.setTask(task);
        subtask.setTitle(request.getTitle());
        subtask.setIsCompleted(false);

        Subtask savedSubtask = subtaskRepository.save(subtask);
        log.info("Subtask created successfully: {}", savedSubtask.getTitle());

        return mapToSubtaskResponse(savedSubtask);
    }

    /**
     * Get all subtasks for a task
     */
    public List<SubtaskResponse> getSubtasksByTask(UUID taskId) {
        log.info("Fetching subtasks for task: {}", taskId);

        List<Subtask> subtasks = subtaskRepository.findByTaskId(taskId);

        return subtasks.stream()
                .map(this::mapToSubtaskResponse)
                .collect(Collectors.toList());
    }

    /**
     * Toggle subtask completion status
     */
    @Transactional
    public SubtaskResponse toggleSubtask(UUID subtaskId) {
        log.info("Toggling subtask completion: {}", subtaskId);

        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new RuntimeException("Subtask not found with id: " + subtaskId));

        // Toggle completion status
        boolean newStatus = !subtask.getIsCompleted();
        subtask.setIsCompleted(newStatus);

        // Set or clear completedAt timestamp
        if (newStatus) {
            subtask.setCompletedAt(LocalDateTime.now());
            log.info("Subtask marked as completed: {}", subtask.getTitle());
        } else {
            subtask.setCompletedAt(null);
            log.info("Subtask marked as incomplete: {}", subtask.getTitle());
        }

        Subtask savedSubtask = subtaskRepository.save(subtask);

        // Calculate and log task completion percentage
        updateTaskCompletionPercentage(subtask.getTask().getId());

        return mapToSubtaskResponse(savedSubtask);
    }

    /**
     * Delete a subtask
     */
    @Transactional
    public void deleteSubtask(UUID subtaskId) {
        log.info("Deleting subtask: {}", subtaskId);

        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new RuntimeException("Subtask not found with id: " + subtaskId));

        UUID taskId = subtask.getTask().getId();
        subtaskRepository.delete(subtask);

        log.info("Subtask deleted successfully");

        // Update task completion percentage after deletion
        updateTaskCompletionPercentage(taskId);
    }

    /**
     * Helper method to update task completion percentage based on subtasks
     */
    private void updateTaskCompletionPercentage(UUID taskId) {
        long totalSubtasks = subtaskRepository.countByTaskId(taskId);
        long completedSubtasks = subtaskRepository.countByTaskIdAndIsCompleted(taskId, true);

        if (totalSubtasks > 0) {
            double percentage = (completedSubtasks * 100.0) / totalSubtasks;
            log.debug("Task {} completion: {}/{} subtasks ({}%)",
                    taskId, completedSubtasks, totalSubtasks, Math.round(percentage));
        }
    }

    // Helper methods

    private SubtaskResponse mapToSubtaskResponse(Subtask subtask) {
        return SubtaskResponse.builder()
                .id(subtask.getId())
                .taskId(subtask.getTask().getId())
                .title(subtask.getTitle())
                .isCompleted(subtask.getIsCompleted())
                .completedAt(subtask.getCompletedAt())
                .build();
    }
}
