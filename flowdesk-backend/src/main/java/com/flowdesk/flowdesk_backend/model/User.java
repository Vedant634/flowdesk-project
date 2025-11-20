package com.flowdesk.flowdesk_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowdesk.flowdesk_backend.model.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_email", columnNames = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    @Email(message = "Email must be valid")
    @NotNull(message = "Email cannot be null")
    private String email;

    @Column(name = "password_hash", nullable = false)
    @NotNull(message = "Password hash cannot be null")
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    @NotNull(message = "First name cannot be null")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotNull(message = "Last name cannot be null")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Role cannot be null")
    private UserRole role;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "skills", columnDefinition = "text[]")
    private List<String> skills = new ArrayList<>();

//    @JdbcTypeCode(SqlTypes.VECTOR)
//    @Column(name = "skill_embedding", columnDefinition = "vector(384)")
//    private float[] skillEmbedding;

    @Column(name = "current_workload_points", nullable = false)
    private Integer currentWorkloadPoints = 0;

    @Column(name = "max_capacity_points", nullable = false)
    private Integer maxCapacityPoints = 40;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
