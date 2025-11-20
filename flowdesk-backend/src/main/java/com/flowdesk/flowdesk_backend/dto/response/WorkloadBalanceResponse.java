package com.flowdesk.flowdesk_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkloadBalanceResponse {

    private Boolean isBalanced;
    private Double imbalanceScore;
    private List<ReassignmentSuggestionResponse> suggestions;
}
