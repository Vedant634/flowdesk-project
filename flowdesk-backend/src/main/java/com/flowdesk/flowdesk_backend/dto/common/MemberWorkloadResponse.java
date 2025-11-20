package com.flowdesk.flowdesk_backend.dto.common;

import com.flowdesk.flowdesk_backend.dto.response.TaskSummaryResponse;
import com.flowdesk.flowdesk_backend.dto.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberWorkloadResponse {

    private UserResponse user;
    private Integer currentWorkload;
    private Integer maxCapacity;
    private Double utilizationPercentage;
    private List<TaskSummaryResponse> activeTasks;
}
