package com.flowdesk.flowdesk_backend.controller;

import com.flowdesk.flowdesk_backend.dto.request.LoginRequest;
import com.flowdesk.flowdesk_backend.dto.request.RegisterRequest;
import com.flowdesk.flowdesk_backend.dto.request.UpdatePasswordRequest;
import com.flowdesk.flowdesk_backend.dto.response.AuthResponse;
import com.flowdesk.flowdesk_backend.dto.response.UserResponse;
import com.flowdesk.flowdesk_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles user registration, login, and authentication operations
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user
     * Public endpoint - no authentication required
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login user
     * Public endpoint - no authentication required
     * Returns JWT token on successful authentication
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current authenticated user
     * Requires authentication
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.info("Get current user request");
        UserResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }

    /**
     * Update user password
     * Requires authentication
     */
    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        log.info("Update password request");
        authService.updatePassword(request);
        return ResponseEntity.noContent().build();
    }
}
