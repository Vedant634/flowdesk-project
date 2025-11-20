package com.flowdesk.flowdesk_backend.util;

import com.flowdesk.flowdesk_backend.exception.UnauthorizedException;
import com.flowdesk.flowdesk_backend.model.User;
import com.flowdesk.flowdesk_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Security utility class for accessing current authenticated user information
 * Provides helper methods to retrieve user details from Spring Security context
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Get current authenticated user's email from SecurityContext
     *
     * @return Email of the currently authenticated user
     * @throws UnauthorizedException if user is not authenticated
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No authentication found in SecurityContext");
            throw new UnauthorizedException("User not authenticated");
        }

        String email = authentication.getName();

        if (email == null || email.equals("anonymousUser")) {
            log.error("Anonymous user attempting to access protected resource");
            throw new UnauthorizedException("User not authenticated");
        }

        log.debug("Retrieved current user email from SecurityContext: {}", email);
        return email;
    }

    /**
     * Get current authenticated user entity from database
     *
     * @return User entity of the currently authenticated user
     * @throws UnauthorizedException if user is not authenticated or not found
     */
    public User getCurrentUser() {
        String email = getCurrentUserEmail();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found in database for authenticated email: {}", email);
                    return new UnauthorizedException("User not found");
                });
    }

    /**
     * Get current authenticated user's ID
     *
     * @return UUID of the currently authenticated user
     * @throws UnauthorizedException if user is not authenticated or not found
     */
    public UUID getCurrentUserId() {
        User user = getCurrentUser();
        UUID userId = user.getId();
        log.debug("Retrieved current user ID: {}", userId);
        return userId;
    }

    /**
     * Check if a user is currently authenticated
     *
     * @return true if user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            String name = authentication.getName();
            boolean authenticated = name != null && !name.equals("anonymousUser");

            log.debug("Authentication check: {}", authenticated);
            return authenticated;

        } catch (Exception e) {
            log.error("Error checking authentication status: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get current user's role
     * Convenience method to check user's role
     *
     * @return UserRole enum of current user
     * @throws UnauthorizedException if user is not authenticated
     */
    public com.flowdesk.flowdesk_backend.model.enums.UserRole getCurrentUserRole() {
        User user = getCurrentUser();
        return user.getRole();
    }

    /**
     * Check if current user is a MANAGER
     *
     * @return true if current user has MANAGER role
     */
    public boolean isManager() {
        try {
            return getCurrentUserRole() == com.flowdesk.flowdesk_backend.model.enums.UserRole.MANAGER;
        } catch (UnauthorizedException e) {
            return false;
        }
    }

    /**
     * Check if current user is a DEVELOPER
     *
     * @return true if current user has DEVELOPER role
     */
    public boolean isDeveloper() {
        try {
            return getCurrentUserRole() == com.flowdesk.flowdesk_backend.model.enums.UserRole.DEVELOPER;
        } catch (UnauthorizedException e) {
            return false;
        }
    }
}
