package com.flowdesk.flowdesk_backend.service;

import com.flowdesk.flowdesk_backend.dto.request.CreateCommentRequest;
import com.flowdesk.flowdesk_backend.dto.response.CommentResponse;
import com.flowdesk.flowdesk_backend.dto.response.UserResponse;
import com.flowdesk.flowdesk_backend.model.Comment;
import com.flowdesk.flowdesk_backend.model.Task;
import com.flowdesk.flowdesk_backend.model.User;
import com.flowdesk.flowdesk_backend.repository.CommentRepository;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    /**
     * Create a new comment on a task
     */
    @Transactional
    public CommentResponse createComment(CreateCommentRequest request, UUID userId) {
        log.info("Creating comment on task: {} by user: {}", request.getTaskId(), userId);

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + request.getTaskId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(user);
        comment.setContent(request.getContent());

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created successfully by user: {}", user.getEmail());

        return mapToCommentResponse(savedComment);
    }

    /**
     * Get all comments for a task (ordered by creation time ascending)
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTask(UUID taskId) {
        log.info("Fetching comments for task: {}", taskId);

        // Verify task exists
        taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        List<Comment> comments = commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);

        return comments.stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update comment content
     */
    @Transactional
    public CommentResponse updateComment(UUID commentId, String newContent) {
        log.info("Updating comment: {}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        comment.setContent(newContent);
        Comment updatedComment = commentRepository.save(comment);

        log.info("Comment updated successfully");

        return mapToCommentResponse(updatedComment);
    }

    /**
     * Delete a comment
     */
    @Transactional
    public void deleteComment(UUID commentId) {
        log.info("Deleting comment: {}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        commentRepository.delete(comment);
        log.info("Comment deleted successfully");
    }

    // Helper methods

    private CommentResponse mapToCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .taskId(comment.getTask().getId())
                .user(mapToUserResponse(comment.getUser()))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
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
