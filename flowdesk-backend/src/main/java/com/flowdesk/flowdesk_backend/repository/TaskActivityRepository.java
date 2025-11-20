package com.flowdesk.flowdesk_backend.repository;

import com.flowdesk.flowdesk_backend.model.TaskActivity;
import com.flowdesk.flowdesk_backend.model.enums.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskActivityRepository extends JpaRepository<TaskActivity, UUID> {

    List<TaskActivity> findByTaskId(UUID taskId);

    List<TaskActivity> findByTaskIdOrderByCreatedAtAsc(UUID taskId);

    List<TaskActivity> findByUserId(UUID userId);

    List<TaskActivity> findByTaskIdAndActivityType(UUID taskId, ActivityType activityType);
}
