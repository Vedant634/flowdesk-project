package com.flowdesk.flowdesk_backend.service;

import com.flowdesk.flowdesk_backend.dto.common.MemberWorkloadResponse;
import com.flowdesk.flowdesk_backend.dto.request.AddTeamMemberRequest;
import com.flowdesk.flowdesk_backend.dto.request.CreateTeamRequest;
import com.flowdesk.flowdesk_backend.dto.request.UpdateTeamRequest;
import com.flowdesk.flowdesk_backend.dto.response.TeamResponse;
import com.flowdesk.flowdesk_backend.dto.response.TeamWorkloadResponse;
import com.flowdesk.flowdesk_backend.dto.response.UserResponse;
import com.flowdesk.flowdesk_backend.model.Team;
import com.flowdesk.flowdesk_backend.model.TeamMember;
import com.flowdesk.flowdesk_backend.model.User;
import com.flowdesk.flowdesk_backend.repository.TeamMemberRepository;
import com.flowdesk.flowdesk_backend.repository.TeamRepository;
import com.flowdesk.flowdesk_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Create a new team
     */
    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request, UUID managerId) {
        log.info("Creating team: {} for manager: {}", request.getName(), managerId);

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found with id: " + managerId));

        Team team = new Team();
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setManager(manager);

        Team savedTeam = teamRepository.save(team);
        log.info("Team created successfully: {}", savedTeam.getName());

        return mapToTeamResponse(savedTeam);
    }

    /**
     * Get team by ID
     */
    @Transactional(readOnly = true)
    public TeamResponse getTeamById(UUID id) {
        log.info("Fetching team with id: {}", id);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));
        return mapToTeamResponse(team);
    }

    /**
     * Get all teams managed by a specific manager
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByManager(UUID managerId) {
        log.info("Fetching teams for manager: {}", managerId);
        List<Team> teams = teamRepository.findByManagerId(managerId);
        return teams.stream()
                .map(this::mapToTeamResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update team information
     */
    @Transactional
    public TeamResponse updateTeam(UUID id, UpdateTeamRequest request) {
        log.info("Updating team with id: {}", id);

        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));

        team.setName(request.getName());
        team.setDescription(request.getDescription());

        Team updatedTeam = teamRepository.save(team);
        log.info("Team updated successfully: {}", updatedTeam.getName());

        return mapToTeamResponse(updatedTeam);
    }

    /**
     * Add a member to the team
     */
    @Transactional
    public TeamResponse addMember(UUID teamId, AddTeamMemberRequest request) {
        log.info("Adding member {} to team {}", request.getUserId(), teamId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        // Check if user is already a member
        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, request.getUserId())) {
            throw new RuntimeException("User is already a member of this team");
        }

        TeamMember teamMember = new TeamMember();
        teamMember.setTeam(team);
        teamMember.setUser(user);

        teamMemberRepository.save(teamMember);
        log.info("Member added successfully: {} to team: {}", user.getEmail(), team.getName());

        return mapToTeamResponse(team);
    }

    /**
     * Remove a member from the team
     */
    @Transactional
    public void removeMember(UUID teamId, UUID userId) {
        log.info("Removing member {} from team {}", userId, teamId);

        TeamMember teamMember = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new RuntimeException("Member not found in team"));

        teamMemberRepository.delete(teamMember);
        log.info("Member removed successfully from team");
    }

    /**
     * Get all members of a team
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getTeamMembers(UUID teamId) {
        log.info("Fetching members for team: {}", teamId);

        List<TeamMember> teamMembers = teamMemberRepository.findByTeamId(teamId);

        return teamMembers.stream()
                .map(tm -> userService.getUserById(tm.getUser().getId()))
                .collect(Collectors.toList());
    }

    /**
     * Get team workload with all members' workload information
     */
    @Transactional(readOnly = true)
    public TeamWorkloadResponse getTeamWorkload(UUID teamId) {
        log.info("Fetching workload for team: {}", teamId);

        List<TeamMember> teamMembers = teamMemberRepository.findByTeamId(teamId);

        List<MemberWorkloadResponse> memberWorkloads = teamMembers.stream()
                .map(tm -> {
                    User user = tm.getUser();
                    double utilization = (user.getCurrentWorkloadPoints() * 100.0) / user.getMaxCapacityPoints();

                    return MemberWorkloadResponse.builder()
                            .user(mapToUserResponse(user))
                            .currentWorkload(user.getCurrentWorkloadPoints())
                            .maxCapacity(user.getMaxCapacityPoints())
                            .utilizationPercentage(Math.round(utilization * 100.0) / 100.0)
                            .activeTasks(List.of()) // Will be populated if needed
                            .build();
                })
                .collect(Collectors.toList());

        // Calculate average utilization
        double averageUtilization = memberWorkloads.stream()
                .mapToDouble(MemberWorkloadResponse::getUtilizationPercentage)
                .average()
                .orElse(0.0);

        // Check if workload is balanced (max deviation < 20%)
        double maxUtilization = memberWorkloads.stream()
                .mapToDouble(MemberWorkloadResponse::getUtilizationPercentage)
                .max()
                .orElse(0.0);
        double minUtilization = memberWorkloads.stream()
                .mapToDouble(MemberWorkloadResponse::getUtilizationPercentage)
                .min()
                .orElse(0.0);
        boolean isBalanced = (maxUtilization - minUtilization) < 20.0;

        return TeamWorkloadResponse.builder()
                .members(memberWorkloads)
                .isBalanced(isBalanced)
                .averageUtilization(Math.round(averageUtilization * 100.0) / 100.0)
                .build();
    }

    // Helper methods

    private TeamResponse mapToTeamResponse(Team team) {
        int memberCount = teamMemberRepository.findByTeamId(team.getId()).size();

        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .manager(mapToUserResponse(team.getManager()))
                .memberCount(memberCount)
                .createdAt(team.getCreatedAt())
                .build();
    }

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
