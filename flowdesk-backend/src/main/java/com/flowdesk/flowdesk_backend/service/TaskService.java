package com.flowdesk.flowdesk_backend.service;

import com.flowdesk.flowdesk_backend.dto.request.ApproveTaskRequest;
import com.flowdesk.flowdesk_backend.dto.request.AssignTaskRequest;
import com.flowdesk.flowdesk_backend.dto.request.CreateTaskRequest;
import com.flowdesk.flowdesk_backend.dto.request.SubmitTaskForReviewRequest;
import com.flowdesk.flowdesk_backend.dto.request.UpdateTaskRequest;
import com.flowdesk.flowdesk_backend.dto.response.TaskResponse;
import com.flowdesk.flowdesk_backend.dto.response.UserResponse;
import com.flowdesk.flowdesk_backend.model.Project;
import com.flowdesk.flowdesk_backend.model.Task;
import com.flowdesk.flowdesk_backend.model.User;
import com.flowdesk.flowdesk_backend.model.enums.TaskStatus;
import com.flowdesk.flowdesk_backend.repository.ProjectRepository;
import com.flowdesk.flowdesk_backend.repository.TaskRepository;
import com.flowdesk.flowdesk_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * Create a new task
     */
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, UUID createdByUserId) {
        log.info("Creating task: {} for project: {}", request.getTitle(), request.getProjectId());

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + request.getProjectId()));

        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + createdByUserId));

        Task task = new Task();
        task.setProject(project);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.TODO);
        task.setPriority(request.getPriority());
        task.setStoryPoints(request.getStoryPoints());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setDueDate(request.getDueDate());
        task.setActualHoursLogged(0);
        task.setCreatedByUser(createdBy);

        // Generate suggested branch name
        String branchName = generateBranchName(request.getTitle());
        task.setSuggestedBranchName(branchName);

        // Assign task if assignee is provided
        if (request.getAssignedToUserId() != null) {
            User assignee = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            task.setAssignedToUser(assignee);
            task.setStartDate(LocalDate.now());

            // Update assignee workload
            assignee.setCurrentWorkloadPoints(
                    assignee.getCurrentWorkloadPoints() + request.getStoryPoints()
            );
            userRepository.save(assignee);
        }

        // Update project total story points
        project.setTotalStoryPoints(project.getTotalStoryPoints() + request.getStoryPoints());
        projectRepository.save(project);

        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully: {}", savedTask.getTitle());

        return mapToTaskResponse(savedTask);
    }

    /**
     * Get task by ID
     */
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID id) {
        log.info("Fetching task with id: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        return mapToTaskResponse(task);
    }

    @Transactional(readOnly = true)
    public Task getTaskEntityById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }


    /**
     * Get all tasks for a project
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(UUID projectId) {
        log.info("Fetching tasks for project: {}", projectId);
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all tasks assigned to a user
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByUser(UUID userId) {
        log.info("Fetching tasks for user: {}", userId);
        List<Task> tasks = taskRepository.findByAssignedToUserId(userId);
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update task information
     */
    @Transactional
    public TaskResponse updateTask(UUID id, UpdateTaskRequest request) {
        log.info("Updating task with id: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        // Update story points and adjust project totals if changed
        if (!task.getStoryPoints().equals(request.getStoryPoints())) {
            int difference = request.getStoryPoints() - task.getStoryPoints();
            Project project = task.getProject();
            project.setTotalStoryPoints(project.getTotalStoryPoints() + difference);
            projectRepository.save(project);

            // Update assignee workload if task is assigned
            if (task.getAssignedToUser() != null) {
                User assignee = task.getAssignedToUser();
                assignee.setCurrentWorkloadPoints(
                        assignee.getCurrentWorkloadPoints() + difference
                );
                userRepository.save(assignee);
            }
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStoryPoints(request.getStoryPoints());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setDueDate(request.getDueDate());

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated successfully: {}", updatedTask.getTitle());

        return mapToTaskResponse(updatedTask);
    }

    /**
     * Assign task to a user
     */
    @Transactional
    public TaskResponse assignTask(UUID taskId, AssignTaskRequest request) {
        log.info("Assigning task {} to user {}", taskId, request.getUserId());

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        User newAssignee = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        // Remove from old assignee's workload if exists
        if (task.getAssignedToUser() != null) {
            User oldAssignee = task.getAssignedToUser();
            oldAssignee.setCurrentWorkloadPoints(
                    oldAssignee.getCurrentWorkloadPoints() - task.getStoryPoints()
            );
            userRepository.save(oldAssignee);
        }

        // Assign to new user and update workload
        task.setAssignedToUser(newAssignee);
        task.setStartDate(LocalDate.now());

        newAssignee.setCurrentWorkloadPoints(
                newAssignee.getCurrentWorkloadPoints() + task.getStoryPoints()
        );
        userRepository.save(newAssignee);

        Task savedTask = taskRepository.save(task);
        log.info("Task assigned successfully to: {}", newAssignee.getEmail());

        return mapToTaskResponse(savedTask);
    }

    /**
     * Update task status
     */
    @Transactional
    public TaskResponse updateTaskStatus(UUID taskId, TaskStatus newStatus) {
        log.info("Updating status of task {} to {}", taskId, newStatus);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);

        // If completing task, update project completed story points and user workload
        if (newStatus == TaskStatus.DONE && oldStatus != TaskStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());

            // Update project progress
            Project project = task.getProject();
            project.setCompletedStoryPoints(
                    project.getCompletedStoryPoints() + task.getStoryPoints()
            );
            projectRepository.save(project);

            // Update user workload
            if (task.getAssignedToUser() != null) {
                User assignee = task.getAssignedToUser();
                assignee.setCurrentWorkloadPoints(
                        assignee.getCurrentWorkloadPoints() - task.getStoryPoints()
                );
                userRepository.save(assignee);
            }
        }

        Task savedTask = taskRepository.save(task);
        log.info("Task status updated from {} to {}", oldStatus, newStatus);

        return mapToTaskResponse(savedTask);
    }

    /**
     * Submit task for review
     */
    @Transactional
    public TaskResponse submitForReview(UUID taskId, SubmitTaskForReviewRequest request) {
        log.info("Submitting task {} for review", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        task.setPullRequestUrl(request.getPullRequestUrl());
        task.setActualHoursLogged(request.getActualHoursLogged());
        task.setStatus(TaskStatus.IN_REVIEW);
        task.setSubmittedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);
        log.info("Task submitted for review: {}", savedTask.getTitle());

        return mapToTaskResponse(savedTask);
    }

    /**
     * Approve task (mark as DONE)
     */
    @Transactional
    public TaskResponse approveTask(UUID taskId, ApproveTaskRequest request) {
        log.info("Approving task {}", taskId);
        return updateTaskStatus(taskId, TaskStatus.DONE);
    }

    /**
     * Request changes on task (move back to IN_PROGRESS)
     */
    @Transactional
    public TaskResponse requestChanges(UUID taskId, String comment) {
        log.info("Requesting changes on task {}", taskId);
        return updateTaskStatus(taskId, TaskStatus.IN_PROGRESS);
    }

    /**
     * Get upcoming deadlines for a user
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getUpcomingDeadlines(UUID userId, int days) {
        log.info("Fetching upcoming deadlines for user {} within {} days", userId, days);

        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(days);

        List<Task> tasks = taskRepository.findByAssignedToUserIdAndDueDateBetween(
                userId, today, futureDate
        );

        return tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    // Helper methods

    private String generateBranchName(String title) {
        // Convert title to kebab-case branch name
        return "feature/" + title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .substring(0, Math.min(50, title.length()));
    }

    private TaskResponse mapToTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .storyPoints(task.getStoryPoints())
                .assignedTo(task.getAssignedToUser() != null ? mapToUserResponse(task.getAssignedToUser()) : null)
                .estimatedHours(task.getEstimatedHours())
                .actualHoursLogged(task.getActualHoursLogged())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())
                .pullRequestUrl(task.getPullRequestUrl())
                .riskScore(task.getRiskScore())
                .riskLevel(task.getRiskLevel())
                .willMissDeadlinePrediction(task.getWillMissDeadlinePrediction())
                .aiGeneratedSummary(task.getAiGeneratedSummary())
                .createdBy(mapToUserResponse(task.getCreatedByUser()))
                .createdAt(task.getCreatedAt())
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
