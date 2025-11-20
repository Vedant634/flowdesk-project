package com.flowdesk.flowdesk_backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitTaskForReviewRequest {

    @NotBlank(message = "Pull request URL is required")
    private String pullRequestUrl;

    @Min(value = 0, message = "Actual hours logged cannot be negative")
    private Integer actualHoursLogged;

    private String comment;
}
