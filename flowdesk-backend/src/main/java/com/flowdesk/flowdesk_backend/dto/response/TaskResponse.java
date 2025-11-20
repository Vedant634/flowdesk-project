package com.flowdesk.flowdesk_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flowdesk.flowdesk_backend.model.enums.RiskLevel;
import com.flowdesk.flowdesk_backend.model.enums.TaskPriority;
import com.flowdesk.flowdesk_backend.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResponse {

    private UUID id;
    private UUID projectId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Integer storyPoints;
    private UserResponse assignedTo;
    private Integer estimatedHours;
    private Integer actualHoursLogged;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDateTime completedAt;
    private String pullRequestUrl;
    private BigDecimal riskScore;
    private RiskLevel riskLevel;
    private Boolean willMissDeadlinePrediction;
    private String aiGeneratedSummary;
    private UserResponse createdBy;
    private LocalDateTime createdAt;
}
