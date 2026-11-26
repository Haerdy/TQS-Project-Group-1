package com.tahelaev.tahelaev.repository;

import com.tahelaev.tahelaev.domain.Robot;
import com.tahelaev.tahelaev.enums.RobotLifecycle;
import com.tahelaev.tahelaev.enums.RobotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RobotRepository extends JpaRepository<Robot, UUID> {
    List<Robot> findByFleetId(UUID fleetId);
    List<Robot> findByFleetIdAndLifecycle(UUID fleetId, RobotLifecycle lifecycle);
    List<Robot> findByFleetIdAndStatus(UUID fleetId, RobotStatus status);
    List<Robot> findByFleetIdAndLifecycleAndStatus(UUID fleetId, RobotLifecycle lifecycle, RobotStatus status);
    Optional<Robot> findByIdAndFleetId(UUID id, UUID fleetId);
    boolean existsByFleetIdAndNameIgnoreCase(UUID fleetId, String name);
}
