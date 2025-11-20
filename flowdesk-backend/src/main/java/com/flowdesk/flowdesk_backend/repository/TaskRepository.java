package com.flowdesk.flowdesk_backend.repository;

import com.flowdesk.flowdesk_backend.model.Task;
import com.flowdesk.flowdesk_backend.model.enums.RiskLevel;
import com.flowdesk.flowdesk_backend.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByProjectId(UUID projectId);

    List<Task> findByAssignedToUserId(UUID userId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByProjectIdAndStatus(UUID projectId, TaskStatus status);

    List<Task> findByAssignedToUserIdAndStatus(UUID userId, TaskStatus status);

    List<Task> findByRiskLevel(RiskLevel riskLevel);

    List<Task> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    List<Task> findByAssignedToUserIdAndDueDateBetween(UUID userId, LocalDate start, LocalDate end);

    long countByProjectId(UUID projectId);

    long countByProjectIdAndStatus(UUID projectId, TaskStatus status);
}

