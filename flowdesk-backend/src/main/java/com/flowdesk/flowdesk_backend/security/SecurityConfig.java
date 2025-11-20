package com.flowdesk.flowdesk_backend.security;

import com.flowdesk.flowdesk_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Spring Security Configuration for JWT Authentication
 * Configures authentication, authorization, CORS, and JWT filter
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserRepository userRepository;

    /**
     * Configure security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain...");

        http
                // Disable CSRF (using JWT, stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Set session management to STATELESS
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/h2-console/**").permitAll() // For H2 database console (development only)

                        // All other /api/** endpoints require authentication
                        .requestMatchers("/api/**").authenticated()

                        // Any other request
                        .anyRequest().authenticated()
                )

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Configure authentication provider
                .authenticationProvider(authenticationProvider());

        // Allow frames for H2 console (development only - remove in production)
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        log.info("Security filter chain configured successfully");
        return http.build();
    }

    /**
     * Configure CORS to allow frontend origins
     * Supports React, Vue, Angular development servers
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuring CORS...");

        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origins (frontend URLs)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",    // React default
                "http://localhost:5173",    // Vite default
                "http://localhost:4200"     // Angular default
        ));

        // Allow specific HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Expose Authorization header to frontend
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        // Register CORS configuration for all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS configured for origins: {}", configuration.getAllowedOrigins());
        return source;
    }

    /**
     * UserDetailsService bean - loads user from database
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            log.debug("Loading user by email: {}", email);

            com.flowdesk.flowdesk_backend.model.User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("User not found with email: {}", email);
                        return new UsernameNotFoundException("User not found with email: " + email);
                    });

            // IMPORTANT: Add "ROLE_" prefix to role for Spring Security authorization
            String authority = "ROLE_" + user.getRole().name();
            log.debug("User found: {} with authority: {}", email, authority);

            return User.builder()
                    .username(user.getEmail())
                    .password(user.getPasswordHash())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority(authority)))
                    .build();
        };
    }

    /**
     * Password encoder bean - BCrypt with 10 rounds
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Configuring BCrypt password encoder with strength 10");
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Authentication provider bean
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        log.info("Configuring DAO authentication provider");
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication manager bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.info("Configuring authentication manager");
        return config.getAuthenticationManager();
    }
}
