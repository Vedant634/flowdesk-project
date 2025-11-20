package com.flowdesk.flowdesk_backend.dto.response;

import com.flowdesk.flowdesk_backend.dto.common.MemberWorkloadResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerDashboardResponse {

    private Integer activeProjects;
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer highRiskTasksCount;
    private List<TaskSummaryResponse> upcomingDeadlines;
    private List<MemberWorkloadResponse> teamWorkload;
}
