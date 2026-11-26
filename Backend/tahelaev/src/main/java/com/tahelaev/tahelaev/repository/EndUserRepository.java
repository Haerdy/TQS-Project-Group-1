package com.tahelaev.tahelaev.repository;

import com.tahelaev.tahelaev.domain.EndUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EndUserRepository extends JpaRepository<EndUser, UUID> {
    Optional<EndUser> findByEmail(String email);
    List<EndUser> findByFleetId(UUID fleetId);
}
