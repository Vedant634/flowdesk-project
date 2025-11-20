package com.flowdesk.flowdesk_backend.dto.ml;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AssigneeRecommendationRequest {
    private List<String> taskSkills;
    private List<DeveloperInfo> developers;

    @Data
    @Builder
    public static class DeveloperInfo {
        private UUID id;
        private String name;
        private List<String> skills;
        private Integer currentWorkload;
        private Integer maxCapacity;
        private Double completionRate;
        private Double avgTaskDuration;
    }
}
