package com.flowdesk.flowdesk_backend.dto.ml;

import lombok.Data;

import java.util.Map;

@Data
public class RiskPredictionResponse {
    private String riskLevel; // LOW/MEDIUM/HIGH
    private Integer riskScore;
    private Map<String, Double> probabilities;
    private String confidence;
}
