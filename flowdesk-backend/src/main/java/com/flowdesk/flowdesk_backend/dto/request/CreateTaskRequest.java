package com.flowdesk.flowdesk_backend.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flowdesk.flowdesk_backend.model.enums.TaskPriority;
import jakarta.validation.constraints.*;
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
public class CreateTaskRequest {

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    @NotNull(message = "Story points are required")
    @Min(value = 1, message = "Story points must be at least 1")
    @Max(value = 21, message = "Story points cannot exceed 21")
    private Integer storyPoints;

    @Min(value = 1, message = "Estimated hours must be at least 1")
    private Integer estimatedHours;

    private LocalDate dueDate;

    private UUID assignedToUserId;
}
