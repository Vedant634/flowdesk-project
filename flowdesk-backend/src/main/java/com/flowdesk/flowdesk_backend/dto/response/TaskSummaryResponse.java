package com.flowdesk.flowdesk_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flowdesk.flowdesk_backend.model.enums.RiskLevel;
import com.flowdesk.flowdesk_backend.model.enums.TaskPriority;
import com.flowdesk.flowdesk_backend.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskSummaryResponse {

    private UUID id;
    private String title;
    private TaskStatus status;
    private TaskPriority priority;
    private Integer storyPoints;
    private LocalDate dueDate;
    private RiskLevel riskLevel;
}
