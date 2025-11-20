package com.flowdesk.flowdesk_backend.service;

import com.flowdesk.flowdesk_backend.dto.request.CreateProjectRequest;
import com.flowdesk.flowdesk_backend.dto.request.UpdateProjectRequest;
import com.flowdesk.flowdesk_backend.dto.response.ProjectProgressResponse;
import com.flowdesk.flowdesk_backend.dto.response.ProjectResponse;
import com.flowdesk.flowdesk_backend.dto.response.TeamResponse;
import com.flowdesk.flowdesk_backend.dto.response.UserResponse;
import com.flowdesk.flowdesk_backend.model.Project;
import com.flowdesk.flowdesk_backend.model.Task;
import com.flowdesk.flowdesk_backend.model.Team;
import com.flowdesk.flowdesk_backend.model.User;
import com.flowdesk.flowdesk_backend.model.enums.ProjectStatus;
import com.flowdesk.flowdesk_backend.model.enums.RiskLevel;
import com.flowdesk.flowdesk_backend.model.enums.TaskStatus;
import com.flowdesk.flowdesk_backend.repository.ProjectRepository;
import com.flowdesk.flowdesk_backend.repository.TaskRepository;
import com.flowdesk.flowdesk_backend.repository.TeamRepository;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    /**
     * Create a new project
     */
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, UUID managerId) {
        log.info("Creating project: {} for manager: {}", request.getName(), managerId);

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + request.getTeamId()));

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found with id: " + managerId));

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setTeam(team);
        project.setManager(manager);
        project.setStatus(ProjectStatus.ACTIVE);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setGithubRepoUrl(request.getGithubRepoUrl());
        project.setGithubRepoName(request.getGithubRepoName());
        project.setTotalStoryPoints(0);
        project.setCompletedStoryPoints(0);

        Project savedProject = projectRepository.save(project);
        log.info("Project created successfully: {}", savedProject.getName());

        return mapToProjectResponse(savedProject);
    }

    /**
     * Get project by ID
     */
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id) {
        log.info("Fetching project with id: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        return mapToProjectResponse(project);
    }

    /**
     * Get all projects managed by a specific manager
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByManager(UUID managerId) {
        log.info("Fetching projects for manager: {}", managerId);
        List<Project> projects = projectRepository.findByManagerId(managerId);
        return projects.stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all projects for a specific team
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByTeam(UUID teamId) {
        log.info("Fetching projects for team: {}", teamId);
        List<Project> projects = projectRepository.findByTeamId(teamId);
        return projects.stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update project information
     */
    @Transactional
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest request) {
        log.info("Updating project with id: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }

        if (request.getGithubRepoUrl() != null) {
            project.setGithubRepoUrl(request.getGithubRepoUrl());
        }

        if (request.getGithubRepoName() != null) {
            project.setGithubRepoName(request.getGithubRepoName());
        }

        Project updatedProject = projectRepository.save(project);
        log.info("Project updated successfully: {}", updatedProject.getName());

        return mapToProjectResponse(updatedProject);
    }

    /**
     * Get project progress statistics
     */
    @Transactional(readOnly = true)
    public ProjectProgressResponse getProjectProgress(UUID projectId) {
        log.info("Fetching progress for project: {}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        long totalTasks = taskRepository.countByProjectId(projectId);
        long completedTasks = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.DONE);

        double completionPercentage = totalTasks > 0
                ? (completedTasks * 100.0) / totalTasks
                : 0.0;

        return ProjectProgressResponse.builder()
                .totalTasks((int) totalTasks)
                .completedTasks((int) completedTasks)
                .totalStoryPoints(project.getTotalStoryPoints())
                .completedStoryPoints(project.getCompletedStoryPoints())
                .completionPercentage(Math.round(completionPercentage * 100.0) / 100.0)
                .build();
    }

    /**
     * Update project risk level based on tasks
     */
    @Transactional
    public void updateProjectRiskLevel(UUID projectId) {
        log.info("Updating risk level for project: {}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        List<Task> tasks = taskRepository.findByProjectId(projectId);

        // Count high-risk tasks
        long highRiskCount = tasks.stream()
                .filter(task -> task.getRiskLevel() == RiskLevel.HIGH)
                .count();

        long mediumRiskCount = tasks.stream()
                .filter(task -> task.getRiskLevel() == RiskLevel.MEDIUM)
                .count();

        // Determine project risk level
        RiskLevel projectRisk;
        if (tasks.isEmpty()) {
            projectRisk = RiskLevel.LOW;
        } else if (highRiskCount > tasks.size() * 0.3) {
            projectRisk = RiskLevel.HIGH;
        } else if ((highRiskCount + mediumRiskCount) > tasks.size() * 0.5) {
            projectRisk = RiskLevel.MEDIUM;
        } else {
            projectRisk = RiskLevel.LOW;
        }

        project.setRiskLevel(projectRisk);
        projectRepository.save(project);

        log.info("Project risk level updated to: {}", projectRisk);
    }

    // Helper methods

    private ProjectResponse mapToProjectResponse(Project project) {
        double completionPercentage = project.getTotalStoryPoints() > 0
                ? (project.getCompletedStoryPoints() * 100.0) / project.getTotalStoryPoints()
                : 0.0;

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .team(mapToTeamResponse(project.getTeam()))
                .manager(mapToUserResponse(project.getManager()))
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .githubRepoUrl(project.getGithubRepoUrl())
                .totalStoryPoints(project.getTotalStoryPoints())
                .completedStoryPoints(project.getCompletedStoryPoints())
                .completionPercentage(Math.round(completionPercentage * 100.0) / 100.0)
                .riskLevel(project.getRiskLevel())
                .createdAt(project.getCreatedAt())
                .build();
    }

    private TeamResponse mapToTeamResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
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
