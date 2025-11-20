package com.flowdesk.flowdesk_backend.dto.ml;

import lombok.Data;

@Data
public class MLSummaryRequest {
    private String title;
    private String description;
    private String task_type;
}
