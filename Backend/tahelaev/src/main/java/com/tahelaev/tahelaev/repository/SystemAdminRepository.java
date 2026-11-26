package com.tahelaev.tahelaev.repository;

import com.tahelaev.tahelaev.domain.SystemAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemAdminRepository extends JpaRepository<SystemAdmin, UUID> {
    Optional<SystemAdmin> findByEmail(String email);
    Optional<SystemAdmin> findByFleetId(UUID fleetId);
}
