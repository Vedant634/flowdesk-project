package com.flowdesk.flowdesk_backend.dto.ml;

import lombok.Data;

import java.util.List;

@Data
public class MLSummaryResponse {
    private String summary;
    private List<String> suggestedSubtasks;
    private String estimatedComplexity;
}
