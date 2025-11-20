package com.flowdesk.flowdesk_backend.repository;

import com.flowdesk.flowdesk_backend.model.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubtaskRepository extends JpaRepository<Subtask, UUID> {

    List<Subtask> findByTaskId(UUID taskId);

    long countByTaskId(UUID taskId);

    long countByTaskIdAndIsCompleted(UUID taskId, boolean isCompleted);
}
