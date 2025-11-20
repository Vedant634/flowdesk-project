package com.flowdesk.flowdesk_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssigneeRecommendationResponse {

    private UserResponse user;
    private Double matchScore;
    private Double skillSimilarity;
    private Double workloadAvailability;
    private String reasoning;
}
