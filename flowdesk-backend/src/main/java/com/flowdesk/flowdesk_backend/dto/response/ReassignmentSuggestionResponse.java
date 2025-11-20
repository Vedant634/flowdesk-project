package com.flowdesk.flowdesk_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReassignmentSuggestionResponse {

    private TaskSummaryResponse task;
    private UserResponse fromUser;
    private UserResponse toUser;
    private String reasoning;
    private Map<String, Integer> expectedBalance;
}
