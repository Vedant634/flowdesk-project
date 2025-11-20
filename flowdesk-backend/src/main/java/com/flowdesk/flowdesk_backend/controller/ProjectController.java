package com.flowdesk.flowdesk_backend.controller;

import com.flowdesk.flowdesk_backend.dto.request.CreateProjectRequest;
import com.flowdesk.flowdesk_backend.dto.request.UpdateProjectRequest;
import com.flowdesk.flowdesk_backend.dto.response.ProjectProgressResponse;
import com.flowdesk.flowdesk_backend.dto.response.ProjectResponse;
import com.flowdesk.flowdesk_backend.dto.response.TaskResponse;
import com.flowdesk.flowdesk_backend.service.ProjectService;
import com.flowdesk.flowdesk_backend.service.TaskService;
import com.flowdesk.flowdesk_backend.util.SecurityUtils;
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
 * Project Controller
 * Handles project management operations
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;
    private final SecurityUtils securityUtils;

    /**
     * Create a new project (Manager only)
     */
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        log.info("Create project request: {}", request.getName());
        UUID managerId = securityUtils.getCurrentUserId();
        ProjectResponse project = projectService.createProject(request, managerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    /**
     * Get all projects
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        log.info("Get all projects request");
        UUID managerId = securityUtils.getCurrentUserId();
        List<ProjectResponse> projects = projectService.getProjectsByManager(managerId);
        return ResponseEntity.ok(projects);
    }

    /**
     * Get project by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID id) {
        log.info("Get project by ID: {}", id);
        ProjectResponse project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    /**
     * Update project information (Manager only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        log.info("Update project request for ID: {}", id);
        ProjectResponse project = projectService.updateProject(id, request);
        return ResponseEntity.ok(project);
    }

    /**
     * Get project progress
     */
    @GetMapping("/{id}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectProgressResponse> getProjectProgress(@PathVariable UUID id) {
        log.info("Get progress for project ID: {}", id);
        ProjectProgressResponse progress = projectService.getProjectProgress(id);
        return ResponseEntity.ok(progress);
    }

    /**
     * Get all tasks for a project
     */
    @GetMapping("/{id}/tasks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskResponse>> getProjectTasks(@PathVariable UUID id) {
        log.info("Get tasks for project ID: {}", id);
        List<TaskResponse> tasks = taskService.getTasksByProject(id);
        return ResponseEntity.ok(tasks);
    }
}
