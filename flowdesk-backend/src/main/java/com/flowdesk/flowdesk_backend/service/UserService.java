package com.flowdesk.flowdesk_backend.service;

import com.flowdesk.flowdesk_backend.dto.request.AddSkillsRequest;
import com.flowdesk.flowdesk_backend.dto.request.UpdateUserRequest;
import com.flowdesk.flowdesk_backend.dto.response.TaskSummaryResponse;
import com.flowdesk.flowdesk_backend.dto.response.UserResponse;
import com.flowdesk.flowdesk_backend.dto.response.WorkloadResponse;
import com.flowdesk.flowdesk_backend.model.Task;
import com.flowdesk.flowdesk_backend.model.User;
import com.flowdesk.flowdesk_backend.model.enums.TaskStatus;
import com.flowdesk.flowdesk_backend.model.enums.UserRole;
import com.flowdesk.flowdesk_backend.repository.TaskRepository;
import com.flowdesk.flowdesk_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    /**
     * Get user by ID
     */
    public UserResponse getUserById(UUID id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    /**
     * Get user by email
     */
    public Optional<User> getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * Update user information
     */
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        if (request.getSkills() != null) {
            user.setSkills(request.getSkills());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getEmail());

        return mapToUserResponse(updatedUser);
    }

    /**
     * Add skills to user profile
     */
    @Transactional
    public UserResponse addSkills(UUID id, AddSkillsRequest request) {
        log.info("Adding skills to user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        List<String> currentSkills = user.getSkills();
        if (currentSkills == null) {
            currentSkills = new ArrayList<>();
        }

        // Add new skills without duplicates
        for (String skill : request.getSkills()) {
            if (!currentSkills.contains(skill)) {
                currentSkills.add(skill);
            }
        }

        user.setSkills(currentSkills);
        User updatedUser = userRepository.save(user);

        log.info("Added {} new skills to user: {}", request.getSkills().size(), user.getEmail());

        return mapToUserResponse(updatedUser);
    }

    /**
     * Get user's workload with active tasks
     */
    public WorkloadResponse getUserWorkload(UUID userId) {
        log.info("Fetching workload for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Get all active tasks (not DONE)
        List<Task> activeTasks = taskRepository.findByAssignedToUserIdAndStatus(userId, TaskStatus.TODO);
        activeTasks.addAll(taskRepository.findByAssignedToUserIdAndStatus(userId, TaskStatus.IN_PROGRESS));
        activeTasks.addAll(taskRepository.findByAssignedToUserIdAndStatus(userId, TaskStatus.IN_REVIEW));

        // Map to task summary responses
        List<TaskSummaryResponse> taskSummaries = activeTasks.stream()
                .map(this::mapToTaskSummaryResponse)
                .collect(Collectors.toList());

        // Calculate utilization percentage
        double utilizationPercentage = (user.getCurrentWorkloadPoints() * 100.0) / user.getMaxCapacityPoints();

        return WorkloadResponse.builder()
                .currentWorkloadPoints(user.getCurrentWorkloadPoints())
                .maxCapacityPoints(user.getMaxCapacityPoints())
                .utilizationPercentage(Math.round(utilizationPercentage * 100.0) / 100.0)
                .activeTasks(taskSummaries)
                .build();
    }

    /**
     * Get all developers
     */
    public List<UserResponse> getAllDevelopers() {
        log.info("Fetching all developers");
        List<User> developers = userRepository.findByRole(UserRole.DEVELOPER);
        return developers.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<User> getAllDeveloperEntities() {
        log.info("Fetching all developer entities");
        return userRepository.findByRole(UserRole.DEVELOPER);
    }

    // Helper methods

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .skills(user.getSkills())
                .currentWorkloadPoints(user.getCurrentWorkloadPoints())
                .maxCapacityPoints(user.getMaxCapacityPoints())
                .createdAt(user.getCreatedAt())
                .build();
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
}
