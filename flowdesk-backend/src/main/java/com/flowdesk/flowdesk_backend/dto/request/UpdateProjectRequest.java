package com.flowdesk.flowdesk_backend.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flowdesk.flowdesk_backend.model.enums.ProjectStatus;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateProjectRequest {

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;

    private ProjectStatus status;

    private LocalDate endDate;

    private String githubRepoUrl;

    private String githubRepoName;
}
