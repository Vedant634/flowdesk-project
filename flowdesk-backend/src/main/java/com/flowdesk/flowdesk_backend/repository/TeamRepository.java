package com.flowdesk.flowdesk_backend.repository;


import com.flowdesk.flowdesk_backend.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

    List<Team> findByManagerId(UUID managerId);

    Optional<Team> findByName(String name);
}

