package com.flowdesk.flowdesk_backend.repository;

import com.flowdesk.flowdesk_backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByTaskId(UUID taskId);

    List<Comment> findByTaskIdOrderByCreatedAtAsc(UUID taskId);

    List<Comment> findByUserId(UUID userId);
}

