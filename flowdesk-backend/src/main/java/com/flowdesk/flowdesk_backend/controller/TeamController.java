package com.flowdesk.flowdesk_backend.controller;

import com.flowdesk.flowdesk_backend.dto.request.AddTeamMemberRequest;
import com.flowdesk.flowdesk_backend.dto.request.CreateTeamRequest;
import com.flowdesk.flowdesk_backend.dto.request.UpdateTeamRequest;
import com.flowdesk.flowdesk_backend.dto.response.TeamResponse;
import com.flowdesk.flowdesk_backend.dto.response.TeamWorkloadResponse;
import com.flowdesk.flowdesk_backend.dto.response.UserResponse;
import com.flowdesk.flowdesk_backend.service.TeamService;
import com.flowdesk.flowdesk_backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Team Controller
 * Handles team management operations
 */
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Slf4j
public class TeamController {

    private final TeamService teamService;
    private final SecurityUtils securityUtils;

    /**
     * Create a new team (Manager only)
     */
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        log.info("Create team request: {}", request.getName());
        UUID managerId = securityUtils.getCurrentUserId();
        TeamResponse team = teamService.createTeam(request, managerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(team);
    }

    /**
     * Get all teams
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        log.info("Get all teams request");
        UUID managerId = securityUtils.getCurrentUserId();
        List<TeamResponse> teams = teamService.getTeamsByManager(managerId);
        return ResponseEntity.ok(teams);
    }

    /**
     * Get team by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable UUID id) {
        log.info("Get team by ID: {}", id);
        TeamResponse team = teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }

    /**
     * Update team information (Manager only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTeamRequest request) {
        log.info("Update team request for ID: {}", id);
        TeamResponse team = teamService.updateTeam(id, request);
        return ResponseEntity.ok(team);
    }

    /**
     * Add member to team (Manager only)
     */
    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TeamResponse> addTeamMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddTeamMemberRequest request) {
        log.info("Add member to team ID: {}", id);
        TeamResponse team = teamService.addMember(id, request);
        return ResponseEntity.ok(team);
    }

    /**
     * Remove member from team (Manager only)
     */
    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> removeTeamMember(
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        log.info("Remove member {} from team {}", userId, id);
        teamService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get team members
     */
    @GetMapping("/{id}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponse>> getTeamMembers(@PathVariable UUID id) {
        log.info("Get members for team ID: {}", id);
        List<UserResponse> members = teamService.getTeamMembers(id);
        return ResponseEntity.ok(members);
    }

    /**
     * Get team workload (Manager only)
     */
    @GetMapping("/{id}/workload")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TeamWorkloadResponse> getTeamWorkload(@PathVariable UUID id) {
        log.info("Get workload for team ID: {}", id);
        TeamWorkloadResponse workload = teamService.getTeamWorkload(id);
        return ResponseEntity.ok(workload);
    }
}
