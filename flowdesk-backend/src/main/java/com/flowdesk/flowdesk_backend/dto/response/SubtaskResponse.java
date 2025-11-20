package com.flowdesk.flowdesk_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubtaskResponse {

    private UUID id;
    private UUID taskId;
    private String title;
    private Boolean isCompleted;
    private LocalDateTime completedAt;
}
