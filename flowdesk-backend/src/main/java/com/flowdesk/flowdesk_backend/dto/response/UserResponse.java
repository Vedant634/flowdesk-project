package com.flowdesk.flowdesk_backend.dto.response;

import com.flowdesk.flowdesk_backend.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private List<String> skills;
    private Integer currentWorkloadPoints;
    private Integer maxCapacityPoints;
    private LocalDateTime createdAt;
}
