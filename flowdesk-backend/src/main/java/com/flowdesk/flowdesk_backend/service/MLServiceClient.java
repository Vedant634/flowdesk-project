package com.flowdesk.flowdesk_backend.service;

import com.flowdesk.flowdesk_backend.dto.ml.*;
import com.flowdesk.flowdesk_backend.model.Task;
import com.flowdesk.flowdesk_backend.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MLServiceClient {

    private final WebClient mlWebClient;

    /**
     * Predicts risk level for a task by calling external ML service.
     */
    public RiskPredictionResponse predictTaskRisk(Task task, User assignedUser) {
        try {
            RiskPredictionRequest request = RiskPredictionRequest.builder()
                    .estimatedHours(task.getEstimatedHours() != null ? task.getEstimatedHours().doubleValue() : 0.0)
                    .storyPoints(task.getStoryPoints() != null ? task.getStoryPoints() : 0)
                    .developerWorkload(calculateDeveloperWorkload(assignedUser))
                    .priority(convertPriority(task.getPriority()))
                    .numSubtasks(task.getSubtasks() != null ? task.getSubtasks().size() : 0)
                    .taskAgeDays(calculateTaskAgeDays(task))
                    .build();

            return mlWebClient.post()
                    .uri("/api/ml/predict-risk")
                    .bodyValue(request)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .bodyToMono(RiskPredictionResponse.class)
                    .block(Duration.ofSeconds(10));
        } catch (Exception e) {
            log.error("Failed to call predict-risk ML service", e);
            RiskPredictionResponse fallback = new RiskPredictionResponse();
            fallback.setRiskLevel("MEDIUM");
            fallback.setRiskScore(50);
            fallback.setProbabilities(Map.of("LOW",0.3,"MEDIUM",0.4,"HIGH",0.3));
            fallback.setConfidence("MEDIUM");
            return fallback;
        }
    }

    /**
     * Recommends assignees for the task by calling external ML service.
     */
    public List<AssigneeRecommendationResponse> recommendAssignee(Task task, List<User> availableDevelopers) {
        try {
            List<String> taskSkills = extractSkillsFromTask(task);
            // --- Make sure DeveloperInfo class is public static in AssigneeRecommendationRequest.java ---
            List<AssigneeRecommendationRequest.DeveloperInfo> developerInfos = availableDevelopers.stream()
                    .map(this::mapUserToDeveloperInfo)
                    .collect(Collectors.toList());

            AssigneeRecommendationRequest request = AssigneeRecommendationRequest.builder()
                    .taskSkills(taskSkills)
                    .developers(developerInfos)
                    .build();

            AssigneeRecommendationResponse[] responseArray = mlWebClient.post()
                    .uri("/api/ml/recommend-assignee")
                    .bodyValue(request)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .bodyToMono(AssigneeRecommendationResponse[].class)
                    .block(Duration.ofSeconds(10));

            return responseArray != null ? List.of(responseArray) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to call recommend-assignee ML service", e);
            return Collections.emptyList();
        }
    }

    /**
     * Generate task summary by calling external ML service.
     */
    public String generateTaskSummary(String title, String description, String taskType) {
        try {
            MLSummaryRequest request = new MLSummaryRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setTask_type(taskType);

            MLSummaryResponse response = mlWebClient.post()
                    .uri("/api/ml/generate-summary")
                    .bodyValue(request)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .bodyToMono(MLSummaryResponse.class)
                    .block(Duration.ofSeconds(10));

            return response != null && response.getSummary() != null ? response.getSummary() : title;
        } catch (Exception e) {
            log.error("Failed to call generate-summary ML service", e);
            return title;
        }
    }

    // Helper: convert TaskPriority enum to ML int priority (1=HIGH,2=MEDIUM,3=LOW)
    private int convertPriority(com.flowdesk.flowdesk_backend.model.enums.TaskPriority priority) {
        if(priority == null) return 2; // Default MEDIUM
        switch(priority) {
            case HIGH: return 1;
            case MEDIUM: return 2;
            case LOW: return 3;
            default: return 2;
        }
    }

    // Helper: calculate developer workload (dummy example, customize logic)
    private double calculateDeveloperWorkload(User user) {
        if(user == null) return 0.0;
        // TODO: Implement workload calculation from user stats
        return 30.0;
    }

    // Helper: calculate task age in days
    private int calculateTaskAgeDays(Task task) {
        if(task.getCreatedAt() == null) return 0;
        return (int) ChronoUnit.DAYS.between(task.getCreatedAt(), java.time.LocalDateTime.now());
    }

    // Helper: extract task skills (dummy - change to business logic)
    private List<String> extractSkillsFromTask(Task task) {
        return List.of("Java", "Spring Boot");
    }

    // Helper: Map User entity to ML DeveloperInfo
    private AssigneeRecommendationRequest.DeveloperInfo mapUserToDeveloperInfo(User user) {
        // Make sure this is public static in AssigneeRecommendationRequest
        return AssigneeRecommendationRequest.DeveloperInfo.builder()
                .id(user.getId())
                .name(user.getFirstName() + " " + user.getLastName())
                .skills(List.of("Java", "Spring Boot"))
                .currentWorkload(20)
                .maxCapacity(40)
                .completionRate(0.9)
                .avgTaskDuration(5.0)
                .build();
    }
}
