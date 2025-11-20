package com.flowdesk.flowdesk_backend.dto.ml;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskPredictionRequest {
    private Double estimatedHours;
    private Integer storyPoints;
    private Double developerWorkload;
    private Integer priority;
    private Integer numSubtasks;
    private Integer taskAgeDays;
}
