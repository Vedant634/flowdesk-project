package com.flowdesk.flowdesk_backend.service;

import com.flowdesk.flowdesk_backend.dto.common.MemberWorkloadResponse;
import com.flowdesk.flowdesk_backend.dto.response.*;
import com.flowdesk.flowdesk_backend.model.Project;
import com.flowdesk.flowdesk_backend.model.Task;
import com.flowdesk.flowdesk_backend.model.TeamMember;
import com.flowdesk.flowdesk_backend.model.User;
import com.flowdesk.flowdesk_backend.model.enums.ProjectStatus;
import com.flowdesk.flowdesk_backend.model.enums.RiskLevel;
import com.flowdesk.flowdesk_backend.model.enums.TaskStatus;
import com.flowdesk.flowdesk_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    /**
     * Get dashboard data for managers
     */
    @Transactional(readOnly = true)
    public ManagerDashboardResponse getManagerDashboard(UUID managerId) {
        log.info("Fetching manager dashboard for user: {}", managerId);

        // Verify user exists
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found with id: " + managerId));

        // Get active projects count
        List<Project> activeProjects = projectRepository.findByManagerIdAndStatus(
                managerId, ProjectStatus.ACTIVE);
        int activeProjectsCount = activeProjects.size();

        // Get all tasks from manager's projects
        List<Task> allTasks = new ArrayList<>();
        for (Project project : activeProjects) {
            allTasks.addAll(taskRepository.findByProjectId(project.getId()));
        }

        int totalTasks = allTasks.size();
        int completedTasks = (int) allTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .count();

        // Get high-risk tasks count
        int highRiskTasksCount = (int) allTasks.stream()
                .filter(task -> task.getRiskLevel() == RiskLevel.HIGH)
                .count();

        // Get upcoming deadlines (next 7 days)
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        List<TaskSummaryResponse> upcomingDeadlines = allTasks.stream()
                .filter(task -> task.getDueDate() != null)
                .filter(task -> !task.getDueDate().isBefore(today) && !task.getDueDate().isAfter(nextWeek))
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .sorted(Comparator.comparing(Task::getDueDate))
                .limit(10)
                .map(this::mapToTaskSummaryResponse)
                .collect(Collectors.toList());

        // Get team workload
        List<MemberWorkloadResponse> teamWorkload = getTeamWorkloadForManager(managerId);

        ManagerDashboardResponse response = ManagerDashboardResponse.builder()
                .activeProjects(activeProjectsCount)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .highRiskTasksCount(highRiskTasksCount)
                .upcomingDeadlines(upcomingDeadlines)
                .teamWorkload(teamWorkload)
                .build();

        log.info("Manager dashboard generated successfully for user: {}", managerId);
        return response;
    }

    /**
     * Get dashboard data for developers
     */
    @Transactional(readOnly = true)
    public DeveloperDashboardResponse getDeveloperDashboard(UUID developerId) {
        log.info("Fetching developer dashboard for user: {}", developerId);

        // Verify user exists
        User developer = userRepository.findById(developerId)
                .orElseThrow(() -> new RuntimeException("Developer not found with id: " + developerId));

        // Get all tasks assigned to developer
        List<Task> myTasks = taskRepository.findByAssignedToUserId(developerId);
        int myTasksCount = myTasks.size();

        // Get current workload
        int currentWorkload = developer.getCurrentWorkloadPoints();

        // Get completed tasks this week
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        int completedThisWeek = (int) myTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .filter(task -> task.getCompletedAt() != null)
                .filter(task -> task.getCompletedAt().isAfter(weekStart))
                .count();

        // Get upcoming deadlines (next 7 days)
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        List<TaskSummaryResponse> upcomingDeadlines = myTasks.stream()
                .filter(task -> task.getDueDate() != null)
                .filter(task -> !task.getDueDate().isBefore(today) && !task.getDueDate().isAfter(nextWeek))
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .sorted(Comparator.comparing(Task::getDueDate))
                .map(this::mapToTaskSummaryResponse)
                .collect(Collectors.toList());

        // Get tasks grouped by status
        Map<TaskStatus, Long> tasksByStatus = myTasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));

        DeveloperDashboardResponse response = DeveloperDashboardResponse.builder()
                .myTasksCount(myTasksCount)
                .currentWorkload(currentWorkload)
                .completedThisWeek(completedThisWeek)
                .upcomingDeadlines(upcomingDeadlines)
                .tasksByStatus(tasksByStatus)
                .build();

        log.info("Developer dashboard generated successfully for user: {}", developerId);
        return response;
    }

    // Helper methods

    /**
     * Get team workload for all members in manager's teams
     */
    @Transactional(readOnly = true)
    private List<MemberWorkloadResponse> getTeamWorkloadForManager(UUID managerId) {
        // Get all team members from manager's teams
        List<Project> managerProjects = projectRepository.findByManagerId(managerId);

        Set<UUID> teamIds = managerProjects.stream()
                .map(project -> project.getTeam().getId())
                .collect(Collectors.toSet());

        List<TeamMember> allTeamMembers = new ArrayList<>();
        for (UUID teamId : teamIds) {
            allTeamMembers.addAll(teamMemberRepository.findByTeamId(teamId));
        }

        // Get unique users from team members
        Set<User> uniqueUsers = allTeamMembers.stream()
                .map(TeamMember::getUser)
                .collect(Collectors.toSet());

        // Create workload response for each user
        return uniqueUsers.stream()
                .map(user -> {
                    double utilization = (user.getCurrentWorkloadPoints() * 100.0) / user.getMaxCapacityPoints();

                    // Get active tasks for this user
                    List<Task> activeTasks = taskRepository.findByAssignedToUserId(user.getId()).stream()
                            .filter(task -> task.getStatus() != TaskStatus.DONE)
                            .collect(Collectors.toList());

                    List<TaskSummaryResponse> taskSummaries = activeTasks.stream()
                            .map(this::mapToTaskSummaryResponse)
                            .collect(Collectors.toList());

                    return MemberWorkloadResponse.builder()
                            .user(mapToUserResponse(user))
                            .currentWorkload(user.getCurrentWorkloadPoints())
                            .maxCapacity(user.getMaxCapacityPoints())
                            .utilizationPercentage(Math.round(utilization * 100.0) / 100.0)
                            .activeTasks(taskSummaries)
                            .build();
                })
                .sorted(Comparator.comparing(MemberWorkloadResponse::getUtilizationPercentage).reversed())
                .collect(Collectors.toList());
    }

    private TaskSummaryResponse mapToTaskSummaryResponse(Task task) {
        return TaskSummaryResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .status(task.getStatus())
                .priority(task.getPriority())
                .storyPoints(task.getStoryPoints())
                .dueDate(task.getDueDate())
                .riskLevel(task.getRiskLevel())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .currentWorkloadPoints(user.getCurrentWorkloadPoints())
                .maxCapacityPoints(user.getMaxCapacityPoints())
                .build();
    }
}
