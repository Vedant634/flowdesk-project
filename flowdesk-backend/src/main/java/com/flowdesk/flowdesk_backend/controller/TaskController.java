package com.flowdesk.flowdesk_backend.controller;

import com.flowdesk.flowdesk_backend.dto.ml.AssigneeRecommendationResponse;
import com.flowdesk.flowdesk_backend.dto.ml.RiskPredictionResponse;
import com.flowdesk.flowdesk_backend.dto.request.*;
import com.flowdesk.flowdesk_backend.dto.response.TaskResponse;
import com.flowdesk.flowdesk_backend.model.Task;
import com.flowdesk.flowdesk_backend.model.User;
import com.flowdesk.flowdesk_backend.model.enums.TaskStatus;
import com.flowdesk.flowdesk_backend.service.MLServiceClient;
import com.flowdesk.flowdesk_backend.service.TaskService;

import com.flowdesk.flowdesk_backend.service.UserService;
import com.flowdesk.flowdesk_backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Task Controller
 * Handles task management operations with ML service integration
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;
    private final MLServiceClient mlServiceClient;
    private final SecurityUtils securityUtils;
    private final UserService userService;


    // -- Existing endpoints --

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        log.info("Create task request: {}", request.getTitle());
        UUID currentUserId = securityUtils.getCurrentUserId();
        TaskResponse task = taskService.createTask(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable UUID id) {
        log.info("Get task by ID: {}", id);
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        log.info("Update task request for ID: {}", id);
        TaskResponse task = taskService.updateTask(id, request);
        return ResponseEntity.ok(task);
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTaskRequest request) {
        log.info("Assign task {} to user {}", id, request.getUserId());
        TaskResponse task = taskService.assignTask(id, request);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        log.info("Update status for task ID: {}", id);
        TaskStatus newStatus = TaskStatus.valueOf(request.get("status"));
        TaskResponse task = taskService.updateTaskStatus(id, newStatus);
        return ResponseEntity.ok(task);
    }

    @PostMapping("/{id}/submit-for-review")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<TaskResponse> submitForReview(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitTaskForReviewRequest request) {
        log.info("Submit task {} for review", id);
        TaskResponse task = taskService.submitForReview(id, request);
        return ResponseEntity.ok(task);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TaskResponse> approveTask(
            @PathVariable UUID id,
            @RequestBody ApproveTaskRequest request) {
        log.info("Approve task ID: {}", id);
        TaskResponse task = taskService.approveTask(id, request);
        return ResponseEntity.ok(task);
    }

    @PostMapping("/{id}/request-changes")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TaskResponse> requestChanges(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        log.info("Request changes on task ID: {}", id);
        String comment = request.get("comment");
        TaskResponse task = taskService.requestChanges(id, comment);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/users/{userId}/tasks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskResponse>> getUserTasks(@PathVariable UUID userId) {
        log.info("Get tasks for user ID: {}", userId);
        List<TaskResponse> tasks = taskService.getTasksByUser(userId);
        return ResponseEntity.ok(tasks);
    }

    // -- ML endpoints --

    /**
     * Get ML risk prediction for a Task.
     * @param id Task UUID
     * @return RiskPredictionResponse (risk level, score, probabilities, confidence)
     */
    @GetMapping("/{id}/risk")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RiskPredictionResponse> getTaskRisk(@PathVariable UUID id) {
        try {
            Task task = taskService.getTaskEntityById(id);
            User assignedUser = task.getAssignedToUser();
            RiskPredictionResponse mlResponse = mlServiceClient.predictTaskRisk(task, assignedUser);
            log.info("Risk prediction for task {}: {}", id, mlResponse);
            return ResponseEntity.ok(mlResponse);
        } catch (Exception e) {
            log.error("Error getting ML risk prediction for task {}", id, e);
            RiskPredictionResponse fallback = new RiskPredictionResponse();
            fallback.setRiskLevel("MEDIUM");
            fallback.setRiskScore(50);
            fallback.setProbabilities(Collections.emptyMap());
            fallback.setConfidence("MEDIUM");
            return ResponseEntity.ok(fallback);
        }
    }

    /**
     * Recommend top assignees for a Task using ML.
     * @param id Task UUID
     * @return List of AssigneeRecommendationResponse
     */
    @PostMapping("/{id}/recommend-assignees")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AssigneeRecommendationResponse>> recommendAssignees(@PathVariable UUID id) {
        try {
            Task task = taskService.getTaskEntityById(id);
            List<User> developers = userService.getAllDeveloperEntities(); // or your team/project-based dev list
            List<AssigneeRecommendationResponse> recommendations = mlServiceClient.recommendAssignee(task, developers);
            log.info("ML recommended assignees for task {}: {} found", id, recommendations.size());
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("Error getting ML assignee recommendations for task {}", id, e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    /**
     * Get AI-generated summary for a Task using ML.
     * @param id Task UUID
     * @return summary string from ML
     */
    @PostMapping("/{id}/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> getTaskSummary(@PathVariable UUID id) {
        try {
            Task task = taskService.getTaskEntityById(id);
            String summary = mlServiceClient.generateTaskSummary(
                    task.getTitle(),
                    task.getDescription(),
                    (task.getPriority() != null ? task.getPriority().name() : "FEATURE")
            );
            log.info("ML summary for task {} generated", id);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error generating ML summary for task {}", id, e);
            return ResponseEntity.ok("Summary unavailable");
        }
    }

}
