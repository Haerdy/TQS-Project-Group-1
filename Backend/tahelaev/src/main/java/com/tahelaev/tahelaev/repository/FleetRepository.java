package com.tahelaev.tahelaev.repository;

import com.tahelaev.tahelaev.domain.Fleet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FleetRepository extends JpaRepository<Fleet, UUID> {
    Optional<Fleet> findByFleetName(String fleetName);
    Optional<Fleet> findByFleetNameIgnoreCase(String fleetName);
    boolean existsByFleetNameIgnoreCase(String fleetName);
}
