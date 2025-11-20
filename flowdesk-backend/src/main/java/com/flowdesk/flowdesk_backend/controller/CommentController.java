package com.flowdesk.flowdesk_backend.controller;

import com.flowdesk.flowdesk_backend.dto.request.CreateCommentRequest;
import com.flowdesk.flowdesk_backend.dto.response.CommentResponse;
import com.flowdesk.flowdesk_backend.service.CommentService;
import com.flowdesk.flowdesk_backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Comment Controller
 * Handles comment operations on tasks
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;
    private final SecurityUtils securityUtils;

    /**
     * Create a new comment
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CreateCommentRequest request) {
        log.info("Create comment request on task: {}", request.getTaskId());
        UUID currentUserId = securityUtils.getCurrentUserId();
        CommentResponse comment = commentService.createComment(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    /**
     * Get all comments for a task
     */
    @GetMapping("/tasks/{taskId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommentResponse>> getTaskComments(@PathVariable UUID taskId) {
        log.info("Get comments for task ID: {}", taskId);
        List<CommentResponse> comments = commentService.getCommentsByTask(taskId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Update comment content
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        log.info("Update comment ID: {}", id);
        String newContent = request.get("content");
        CommentResponse comment = commentService.updateComment(id, newContent);
        return ResponseEntity.ok(comment);
    }

    /**
     * Delete a comment
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id) {
        log.info("Delete comment ID: {}", id);
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
