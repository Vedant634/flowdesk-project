package com.flowdesk.flowdesk_backend.controller;

import com.flowdesk.flowdesk_backend.dto.request.AddSkillsRequest;
import com.flowdesk.flowdesk_backend.dto.request.UpdateUserRequest;
import com.flowdesk.flowdesk_backend.dto.response.UserResponse;
import com.flowdesk.flowdesk_backend.dto.response.WorkloadResponse;
import com.flowdesk.flowdesk_backend.service.UserService;
import com.flowdesk.flowdesk_backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * User Controller
 * Handles user management operations
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    /**
     * Get all users
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Get all users request");
        List<UserResponse> users = userService.getAllDevelopers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        log.info("Get user by ID: {}", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Update user information
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Update user request for ID: {}", id);
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    /**
     * Add skills to user
     */
    @PostMapping("/{id}/skills")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> addSkills(
            @PathVariable UUID id,
            @Valid @RequestBody AddSkillsRequest request) {
        log.info("Add skills request for user ID: {}", id);
        UserResponse user = userService.addSkills(id, request);
        return ResponseEntity.ok(user);
    }

    /**
     * Get user workload
     */
    @GetMapping("/{id}/workload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkloadResponse> getUserWorkload(@PathVariable UUID id) {
        log.info("Get workload for user ID: {}", id);
        WorkloadResponse workload = userService.getUserWorkload(id);
        return ResponseEntity.ok(workload);
    }
}
