package com.flowdesk.flowdesk_backend.repository;

import com.flowdesk.flowdesk_backend.model.User;
import com.flowdesk.flowdesk_backend.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRole role);

    // CHANGED THIS METHOD - Use native query with PostgreSQL's ANY operator
    @Query(value = "SELECT * FROM users WHERE role = CAST(:role AS text) AND :skill = ANY(skills)",
            nativeQuery = true)
    List<User> findByRoleAndSkillsContaining(@Param("role") String role, @Param("skill") String skill);
}
