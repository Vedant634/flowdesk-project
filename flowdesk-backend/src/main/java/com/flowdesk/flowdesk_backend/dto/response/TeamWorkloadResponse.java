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
public class TeamWorkloadResponse {

    private List<MemberWorkloadResponse> members;
    private Boolean isBalanced;
    private Double averageUtilization;
}
