package com.flowdesk.flowdesk_backend.controller;

import com.flowdesk.flowdesk_backend.dto.request.CreateSubtaskRequest;
import com.flowdesk.flowdesk_backend.dto.response.SubtaskResponse;
import com.flowdesk.flowdesk_backend.service.SubtaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Subtask Controller
 * Handles subtask operations within tasks
 */
@RestController
@RequestMapping("/api/subtasks")
@RequiredArgsConstructor
@Slf4j
public class SubtaskController {

    private final SubtaskService subtaskService;

    /**
     * Create a new subtask
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubtaskResponse> createSubtask(@Valid @RequestBody CreateSubtaskRequest request) {
        log.info("Create subtask request: {}", request.getTitle());
        SubtaskResponse subtask = subtaskService.createSubtask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(subtask);
    }

    /**
     * Get all subtasks for a task
     */
    @GetMapping("/tasks/{taskId}/subtasks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SubtaskResponse>> getTaskSubtasks(@PathVariable UUID taskId) {
        log.info("Get subtasks for task ID: {}", taskId);
        List<SubtaskResponse> subtasks = subtaskService.getSubtasksByTask(taskId);
        return ResponseEntity.ok(subtasks);
    }

    /**
     * Toggle subtask completion
     */
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubtaskResponse> toggleSubtask(@PathVariable UUID id) {
        log.info("Toggle subtask ID: {}", id);
        SubtaskResponse subtask = subtaskService.toggleSubtask(id);
        return ResponseEntity.ok(subtask);
    }

    /**
     * Delete a subtask
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteSubtask(@PathVariable UUID id) {
        log.info("Delete subtask ID: {}", id);
        subtaskService.deleteSubtask(id);
        return ResponseEntity.noContent().build();
    }
}
