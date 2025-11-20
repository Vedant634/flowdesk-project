package com.flowdesk.flowdesk_backend.dto.response;

import com.flowdesk.flowdesk_backend.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeveloperDashboardResponse {

    private Integer myTasksCount;
    private Integer currentWorkload;
    private Integer completedThisWeek;
    private List<TaskSummaryResponse> upcomingDeadlines;
    private Map<TaskStatus, Long> tasksByStatus;
}
