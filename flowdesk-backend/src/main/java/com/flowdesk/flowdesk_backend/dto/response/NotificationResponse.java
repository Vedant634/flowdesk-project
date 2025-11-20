package com.flowdesk.flowdesk_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flowdesk.flowdesk_backend.model.enums.NotificationType;
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
public class NotificationResponse {

    private UUID id;
    private NotificationType type;
    private String title;
    private String message;
    private UUID taskId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
