package com.flowdesk.flowdesk_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubtaskRequest {

    @NotNull(message = "Task ID is required")
    private UUID taskId;

    @NotBlank(message = "Subtask title is required")
    private String title;
}
