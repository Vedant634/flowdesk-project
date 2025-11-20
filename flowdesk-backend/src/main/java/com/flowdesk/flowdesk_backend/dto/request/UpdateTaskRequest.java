package com.flowdesk.flowdesk_backend.dto.request;

import com.flowdesk.flowdesk_backend.model.enums.TaskPriority;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private TaskPriority priority;

    @Min(value = 1, message = "Story points must be at least 1")
    @Max(value = 21, message = "Story points cannot exceed 21")
    private Integer storyPoints;

    @Min(value = 1, message = "Estimated hours must be at least 1")
    private Integer estimatedHours;

    private LocalDate dueDate;
}
