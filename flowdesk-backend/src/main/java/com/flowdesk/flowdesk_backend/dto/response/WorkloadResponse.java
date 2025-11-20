package com.flowdesk.flowdesk_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkloadResponse {

    private Integer currentWorkloadPoints;
    private Integer maxCapacityPoints;
    private Double utilizationPercentage;
    private List<TaskSummaryResponse> activeTasks;
}
