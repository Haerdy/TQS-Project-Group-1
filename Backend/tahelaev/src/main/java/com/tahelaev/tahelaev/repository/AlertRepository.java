package com.tahelaev.tahelaev.repository;

import com.tahelaev.tahelaev.domain.Alert;
import com.tahelaev.tahelaev.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByFleetId(UUID fleetId);
    List<Alert> findByFleetIdAndAcknowledged(UUID fleetId, boolean acknowledged);
    List<Alert> findByFleetIdAndType(UUID fleetId, AlertType type);
    List<Alert> findByRobotId(UUID robotId);
}
