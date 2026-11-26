package com.tahelaev.tahelaev.repository;

import com.tahelaev.tahelaev.domain.Standard;
import com.tahelaev.tahelaev.enums.RobotLifecycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StandardRobotRepository extends JpaRepository<Standard, UUID> {
    List<Standard> findByFleetId(UUID fleetId);
    List<Standard> findByFleetIdAndLifecycle(UUID fleetId, RobotLifecycle lifecycle);
}
