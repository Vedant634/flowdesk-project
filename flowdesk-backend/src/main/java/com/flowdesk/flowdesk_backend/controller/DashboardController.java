package com.flowdesk.flowdesk_backend.controller;

import com.flowdesk.flowdesk_backend.dto.response.DeveloperDashboardResponse;
import com.flowdesk.flowdesk_backend.dto.response.ManagerDashboardResponse;
import com.flowdesk.flowdesk_backend.service.DashboardService;
import com.flowdesk.flowdesk_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Dashboard Controller
 * Provides dashboard data for managers and developers
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;
    private final SecurityUtils securityUtils;

    /**
     * Get manager dashboard (Manager only)
     */
    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ManagerDashboardResponse> getManagerDashboard() {
        log.info("Get manager dashboard");
        UUID managerId = securityUtils.getCurrentUserId();
        ManagerDashboardResponse dashboard = dashboardService.getManagerDashboard(managerId);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get developer dashboard (Developer only)
     */
    @GetMapping("/developer")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<DeveloperDashboardResponse> getDeveloperDashboard() {
        log.info("Get developer dashboard");
        UUID developerId = securityUtils.getCurrentUserId();
        DeveloperDashboardResponse dashboard = dashboardService.getDeveloperDashboard(developerId);
        return ResponseEntity.ok(dashboard);
    }
}
