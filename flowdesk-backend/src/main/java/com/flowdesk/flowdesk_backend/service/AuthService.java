package com.flowdesk.flowdesk_backend.service;

import com.flowdesk.flowdesk_backend.dto.request.LoginRequest;
import com.flowdesk.flowdesk_backend.dto.request.RegisterRequest;
import com.flowdesk.flowdesk_backend.dto.request.UpdatePasswordRequest;
import com.flowdesk.flowdesk_backend.dto.response.AuthResponse;
import com.flowdesk.flowdesk_backend.dto.response.UserResponse;
import com.flowdesk.flowdesk_backend.exception.ConflictException;
import com.flowdesk.flowdesk_backend.exception.ResourceNotFoundException;
import com.flowdesk.flowdesk_backend.exception.UnauthorizedException;
import com.flowdesk.flowdesk_backend.model.User;
import com.flowdesk.flowdesk_backend.repository.UserRepository;
import com.flowdesk.flowdesk_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Register a new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        user.setSkills(request.getSkills() != null ? request.getSkills() : new ArrayList<>());
        user.setCurrentWorkloadPoints(0);
        user.setMaxCapacityPoints(request.getMaxCapacity() != null ? request.getMaxCapacity() : 40);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        // Generate JWT token
        String accessToken = jwtUtil.generateToken(savedUser.getEmail());

        return AuthResponse.builder()
                .user(mapToUserResponse(savedUser))
                .accessToken(accessToken)
                .refreshToken(null)
                .build();
    }

    /**
     * Login user with email and password
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }

        log.info("User logged in successfully: {}", user.getEmail());

        // Generate JWT token
        String accessToken = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .user(mapToUserResponse(user))
                .accessToken(accessToken)
                .refreshToken(null)
                .build();
    }

    /**
     * Get currently authenticated user
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return mapToUserResponse(user);
    }

    /**
     * Update user password
     */
    @Transactional
    public void updatePassword(UpdatePasswordRequest request) {
        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        // Update to new password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password updated successfully for user: {}", user.getEmail());
    }

    /**
     * Get current authenticated user's email from SecurityContext
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        String email = authentication.getName();

        if (email == null || email.equals("anonymousUser")) {
            throw new UnauthorizedException("User not authenticated");
        }

        return email;
    }

    /**
     * Get current authenticated user entity
     */
    public User getCurrentUserEntity() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    // Helper method
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .skills(user.getSkills())
                .currentWorkloadPoints(user.getCurrentWorkloadPoints())
                .maxCapacityPoints(user.getMaxCapacityPoints())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
