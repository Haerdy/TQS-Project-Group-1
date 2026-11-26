package com.tahelaev.tahelaev.repository;

import com.tahelaev.tahelaev.domain.Refrigerated;
import com.tahelaev.tahelaev.enums.RobotLifecycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RefrigeratedRobotRepository extends JpaRepository<Refrigerated, UUID> {
    List<Refrigerated> findByFleetId(UUID fleetId);
    List<Refrigerated> findByFleetIdAndLifecycle(UUID fleetId, RobotLifecycle lifecycle);
}
