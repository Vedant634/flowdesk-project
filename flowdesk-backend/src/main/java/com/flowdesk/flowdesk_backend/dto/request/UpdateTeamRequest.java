package com.flowdesk.flowdesk_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTeamRequest {

    @NotBlank(message = "Team name is required")
    private String name;

    private String description;
}
