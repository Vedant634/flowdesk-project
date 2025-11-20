package com.flowdesk.flowdesk_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.flowdesk.flowdesk_backend.model.enums.RiskLevel;
import com.flowdesk.flowdesk_backend.model.enums.TaskPriority;
import com.flowdesk.flowdesk_backend.model.enums.TaskStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @NotNull(message = "Project cannot be null")
    private Project project;

    @Column(nullable = false)
    @NotNull(message = "Title cannot be null")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Status cannot be null")
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Priority cannot be null")
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "story_points", nullable = false)
    @Min(value = 1, message = "Story points must be at least 1")
    @Max(value = 21, message = "Story points cannot exceed 21")
    @NotNull(message = "Story points cannot be null")
    private Integer storyPoints;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedToUser;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "actual_hours_logged", nullable = false)
    private Integer actualHoursLogged = 0;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "suggested_branch_name")
    private String suggestedBranchName;

    @Column(name = "pull_request_url")
    private String pullRequestUrl;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "risk_score", precision = 5, scale = 4)
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;

    @Column(name = "will_miss_deadline_prediction")
    private Boolean willMissDeadlinePrediction;

    @Column(name = "ai_generated_summary", columnDefinition = "TEXT")
    private String aiGeneratedSummary;

    @Column(name = "summary_generated_at")
    private LocalDateTime summaryGeneratedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    @NotNull(message = "Creator cannot be null")
    private User createdByUser;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subtask> subtasks = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskActivity> activities = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

