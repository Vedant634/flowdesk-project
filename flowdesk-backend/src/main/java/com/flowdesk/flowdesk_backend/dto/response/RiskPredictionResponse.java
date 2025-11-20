package com.flowdesk.flowdesk_backend.dto.response;

import com.flowdesk.flowdesk_backend.model.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskPredictionResponse {

    private UUID taskId;
    private BigDecimal riskScore;
    private RiskLevel riskLevel;
    private Boolean willMissDeadline;
    private Map<String, Double> factors;
    private List<String> recommendations;
}
