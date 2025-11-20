package com.flowdesk.flowdesk_backend.security;

import com.flowdesk.flowdesk_backend.model.User;
import com.flowdesk.flowdesk_backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter
 * Intercepts all requests, validates JWT tokens, and sets authentication context
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract Authorization header
            final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

            // Check if header exists and starts with "Bearer "
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                log.debug("No JWT token found in request to {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // Extract token (remove "Bearer " prefix)
            final String jwt = authHeader.substring(BEARER_PREFIX.length());

            // Extract email from token
            final String userEmail = jwtUtil.extractEmail(jwt);

            log.debug("JWT token found for email: {} in request to {}", userEmail, request.getRequestURI());

            // If email exists and no authentication is set in context
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load user from database
                User user = userRepository.findByEmail(userEmail).orElse(null);

                if (user != null) {
                    // Validate token
                    if (jwtUtil.validateToken(jwt, user.getEmail())) {
                        log.info("Valid JWT token for user: {} (Role: {})", userEmail, user.getRole());

                        // Create authentication token with user details and authorities
                        // IMPORTANT: Add "ROLE_" prefix for Spring Security role-based authorization
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                        );

                        // Set authentication details
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Set authentication in SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.debug("Authentication set in SecurityContext for user: {}", userEmail);
                    } else {
                        log.warn("Invalid JWT token for user: {}", userEmail);
                    }
                } else {
                    log.warn("User not found in database for email: {}", userEmail);
                }
            }

        } catch (Exception e) {
            log.error("Cannot set user authentication in SecurityContext: {}", e.getMessage(), e);
            // Don't throw exception - let request continue to be handled by Spring Security
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
