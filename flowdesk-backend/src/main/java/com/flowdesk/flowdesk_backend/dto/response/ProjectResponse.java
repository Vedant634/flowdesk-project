package com.flowdesk.flowdesk_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flowdesk.flowdesk_backend.model.enums.ProjectStatus;
import com.flowdesk.flowdesk_backend.model.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectResponse {

    private UUID id;
    private String name;
    private String description;
    private TeamResponse team;
    private UserResponse manager;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String githubRepoUrl;
    private Integer totalStoryPoints;
    private Integer completedStoryPoints;
    private Double completionPercentage;
    private RiskLevel riskLevel;
    private LocalDateTime createdAt;
}
