package com.flowdesk.flowdesk_backend.dto.ml;

import lombok.Data;

import java.util.UUID;

@Data
public class AssigneeRecommendationResponse {
    private UUID developerId;
    private String name;
    private Integer skillMatchScore;
    private Integer recommendationScore;
    private String confidence;
    private String reasoning;
}
