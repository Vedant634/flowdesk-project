package com.flowdesk.flowdesk_backend.dto.response;

import com.flowdesk.flowdesk_backend.model.enums.ActivityType;
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
public class TaskActivityResponse {

    private UUID id;
    private UUID taskId;
    private UserResponse user;
    private ActivityType activityType;
    private String description;
    private LocalDateTime createdAt;
}
