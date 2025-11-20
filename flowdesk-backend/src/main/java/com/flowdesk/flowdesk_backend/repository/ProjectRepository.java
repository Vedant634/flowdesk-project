package com.flowdesk.flowdesk_backend.repository;

import com.flowdesk.flowdesk_backend.model.Project;
import com.flowdesk.flowdesk_backend.model.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByTeamId(UUID teamId);

    List<Project> findByManagerId(UUID managerId);

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByManagerIdAndStatus(UUID managerId, ProjectStatus status);
}
