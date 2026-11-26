package com.tahelaev.tahelaev.service;

import com.tahelaev.tahelaev.domain.Alert;
import com.tahelaev.tahelaev.domain.Fleet;
import com.tahelaev.tahelaev.domain.Robot;
import com.tahelaev.tahelaev.domain.SystemAdmin;
import com.tahelaev.tahelaev.dto.AlertDto;
import com.tahelaev.tahelaev.enums.AlertType;
import com.tahelaev.tahelaev.repository.AlertRepository;
import com.tahelaev.tahelaev.repository.FleetRepository;
import com.tahelaev.tahelaev.repository.RobotRepository;
import com.tahelaev.tahelaev.repository.SystemAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final FleetRepository fleetRepository;
    private final RobotRepository robotRepository;
    private final SystemAdminRepository systemAdminRepository;

    @Transactional
    public Alert createAlert(UUID fleetId, UUID robotId, AlertType type, String message, float value) {
        Fleet fleet = fleetRepository.findById(fleetId)
                .orElseThrow(() -> new IllegalArgumentException("Fleet not found with id: " + fleetId));

        Robot robot = null;
        if (robotId != null) {
            robot = robotRepository.findById(robotId).orElse(null);
        }

        Alert alert = Alert.builder()
                .type(type)
                .message(message)
                .value(value)
                .time(LocalDateTime.now())
                .acknowledged(false)
                .fleet(fleet)
                .robot(robot)
                .build();

        return alertRepository.save(alert);
    }

    public List<AlertDto> getAlertsByFleet(UUID fleetId, Boolean unacknowledgedOnly) {
        List<Alert> alerts;
        if (Boolean.TRUE.equals(unacknowledgedOnly)) {
            alerts = alertRepository.findByFleetIdAndAcknowledged(fleetId, false);
        } else {
            alerts = alertRepository.findByFleetId(fleetId);
        }
        return alerts.stream().map(this::toDto).toList();
    }

    @Transactional
    public AlertDto acknowledgeAlert(UUID alertId, UUID systemAdminId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found with id: " + alertId));

        SystemAdmin admin = systemAdminRepository.findById(systemAdminId)
                .orElseThrow(() -> new IllegalArgumentException("System admin not found with id: " + systemAdminId));

        alert.setAcknowledged(true);
        alert.setAckTime(LocalDateTime.now());
        alert.setAcknowledgedBy(admin);

        return toDto(alertRepository.save(alert));
    }

    public AlertDto toDto(Alert alert) {
        return AlertDto.builder()
                .id(alert.getId())
                .type(alert.getType())
                .message(alert.getMessage())
                .value(alert.getValue())
                .time(alert.getTime())
                .acknowledged(alert.isAcknowledged())
                .ackTime(alert.getAckTime())
                .fleetId(alert.getFleet() != null ? alert.getFleet().getId() : null)
                .robotId(alert.getRobot() != null ? alert.getRobot().getId() : null)
                .acknowledgedById(alert.getAcknowledgedBy() != null ? alert.getAcknowledgedBy().getId() : null)
                .build();
    }
}
