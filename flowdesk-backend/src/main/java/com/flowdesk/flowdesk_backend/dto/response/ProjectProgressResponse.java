package com.flowdesk.flowdesk_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectProgressResponse {

    private Integer totalTasks;
    private Integer completedTasks;
    private Integer totalStoryPoints;
    private Integer completedStoryPoints;
    private Double completionPercentage;
}
