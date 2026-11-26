package com.tahelaev.tahelaev.service;

import com.tahelaev.tahelaev.domain.Fleet;
import com.tahelaev.tahelaev.domain.Location;
import com.tahelaev.tahelaev.domain.Refrigerated;
import com.tahelaev.tahelaev.domain.Robot;
import com.tahelaev.tahelaev.domain.Standard;
import com.tahelaev.tahelaev.dto.LocationDto;
import com.tahelaev.tahelaev.dto.RobotCreateRequestDto;
import com.tahelaev.tahelaev.dto.RobotDto;
import com.tahelaev.tahelaev.dto.RobotLifecycleUpdateDto;
import com.tahelaev.tahelaev.dto.RobotTelemetryDto;
import com.tahelaev.tahelaev.enums.AlertType;
import com.tahelaev.tahelaev.enums.RobotLifecycle;
import com.tahelaev.tahelaev.enums.RobotStatus;
import com.tahelaev.tahelaev.repository.FleetRepository;
import com.tahelaev.tahelaev.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RobotService {

    private final RobotRepository robotRepository;
    private final FleetRepository fleetRepository;
    private final AlertService alertService;

    @Transactional
    public RobotDto registerRobot(RobotCreateRequestDto request) {
        if (request.getMaxWeight() == null || request.getMaxWeight() <= 0) {
            throw new IllegalArgumentException("Payload capacity must be greater than 0");
        }

        Fleet fleet = fleetRepository.findById(request.getFleetId())
                .orElseThrow(() -> new IllegalArgumentException("Fleet not found with id: " + request.getFleetId()));

        if (robotRepository.existsByFleetIdAndNameIgnoreCase(fleet.getId(), request.getName())) {
            throw new IllegalArgumentException("Robot name already exists in this hospital: " + request.getName());
        }

        Robot robot;
        if ("REFRIGERATED".equalsIgnoreCase(request.getRobotType())) {
            robot = Refrigerated.builder()
                    .name(request.getName())
                    .fleet(fleet)
                    .status(RobotStatus.OFFLINE)
                    .lifecycle(RobotLifecycle.REGISTERED)
                    .battery(100.0f)
                    .mileageKm(0.0f)
                    .maxSpeedKm(request.getMaxSpeedKm() != null ? request.getMaxSpeedKm() : 20.0f)
                    .maxWeight(request.getMaxWeight())
                    .curWeight(0.0f)
                    .cargoSpaceLiters(request.getCargoSpaceLiters() != null ? request.getCargoSpaceLiters() : 50.0)
                    .minTempC(request.getMinTempC() != null ? request.getMinTempC() : 2.0f)
                    .maxTempC(request.getMaxTempC() != null ? request.getMaxTempC() : 8.0f)
                    .targetTempC(request.getTargetTempC() != null ? request.getTargetTempC() : 4.0f)
                    .currentTempC(request.getTargetTempC() != null ? request.getTargetTempC() : 4.0f)
                    .build();
        } else {
            robot = Standard.builder()
                    .name(request.getName())
                    .fleet(fleet)
                    .status(RobotStatus.OFFLINE)
                    .lifecycle(RobotLifecycle.REGISTERED)
                    .battery(100.0f)
                    .mileageKm(0.0f)
                    .maxSpeedKm(request.getMaxSpeedKm() != null ? request.getMaxSpeedKm() : 20.0f)
                    .maxWeight(request.getMaxWeight())
                    .curWeight(0.0f)
                    .cargoSpaceLiters(request.getCargoSpaceLiters() != null ? request.getCargoSpaceLiters() : 50.0)
                    .build();
        }

        robot = robotRepository.save(robot);
        updateFleetRobotCounts(fleet);

        return toDto(robot);
    }

    @Transactional
    public RobotDto updateLifecycle(UUID robotId, RobotLifecycleUpdateDto request) {
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new IllegalArgumentException("Robot not found with id: " + robotId));

        RobotLifecycle current = robot.getLifecycle();
        RobotLifecycle next = request.getLifecycle();

        if (current == next) {
            return toDto(robot);
        }

        // BR6: Check if allowed transition
        boolean isAllowed = switch (current) {
            case REGISTERED -> next == RobotLifecycle.ACTIVE || next == RobotLifecycle.RETIRED;
            case ACTIVE -> next == RobotLifecycle.OUT_OF_SERVICE || next == RobotLifecycle.RETIRED;
            case OUT_OF_SERVICE -> next == RobotLifecycle.ACTIVE || next == RobotLifecycle.RETIRED;
            case RETIRED -> false; // Retired is final
        };

        if (!isAllowed) {
            throw new IllegalStateException("Transition not allowed");
        }

        // BR6: No change to OUT_OF_SERVICE or RETIRED during an active delivery
        if ((next == RobotLifecycle.OUT_OF_SERVICE || next == RobotLifecycle.RETIRED) && robot.getCurrentParcel() != null) {
            throw new IllegalStateException("Robot has an active delivery");
        }

        robot.setLifecycle(next);

        // When activated, set to IDLE
        if (next == RobotLifecycle.ACTIVE && robot.getStatus() == RobotStatus.OFFLINE) {
            robot.setStatus(RobotStatus.IDLE);
        } else if (next == RobotLifecycle.OUT_OF_SERVICE || next == RobotLifecycle.RETIRED) {
            robot.setStatus(RobotStatus.OFFLINE);
        }

        robot = robotRepository.save(robot);
        updateFleetRobotCounts(robot.getFleet());

        return toDto(robot);
    }

    @Transactional
    public void deleteRobot(UUID robotId) {
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new IllegalArgumentException("Robot not found with id: " + robotId));

        if (robot.getLifecycle() != RobotLifecycle.REGISTERED) {
            throw new IllegalStateException("Retire this robot instead");
        }

        Fleet fleet = robot.getFleet();
        robotRepository.delete(robot);
        updateFleetRobotCounts(fleet);
    }

    @Transactional
    public RobotDto updateTelemetry(UUID robotId, RobotTelemetryDto telemetry) {
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new IllegalArgumentException("Robot not found with id: " + robotId));

        if (telemetry.getLocation() != null) {
            robot.setLocation(new Location(telemetry.getLocation().getLatitude(), telemetry.getLocation().getLongitude()));
        }

        if (telemetry.getMileageDeltaKm() != null && telemetry.getMileageDeltaKm() > 0) {
            robot.setMileageKm(robot.getMileageKm() + telemetry.getMileageDeltaKm());
        }

        if (telemetry.getBattery() != null) {
            robot.setBattery(telemetry.getBattery());

            // BR8: Low battery alert threshold 20%
            if (robot.getBattery() < 20.0f) {
                alertService.createAlert(
                        robot.getFleet().getId(),
                        robot.getId(),
                        AlertType.LOW_BATTERY,
                        String.format("Low battery: %s (%.0f %%)", robot.getName(), robot.getBattery()),
                        robot.getBattery()
                );

                if (robot.getStatus() == RobotStatus.IDLE) {
                    robot.setStatus(RobotStatus.CHARGING);
                }
            } else if (robot.getBattery() >= 100.0f && robot.getStatus() == RobotStatus.CHARGING) {
                robot.setStatus(RobotStatus.IDLE);
            }
        }

        if (robot instanceof Refrigerated refrigerated && telemetry.getCurrentTempC() != null) {
            refrigerated.setCurrentTempC(telemetry.getCurrentTempC());

            // BR9: Refrigerated safe range 2.0 to 8.0 °C
            if (refrigerated.getCurrentTempC() < 2.0f || refrigerated.getCurrentTempC() > 8.0f) {
                alertService.createAlert(
                        robot.getFleet().getId(),
                        robot.getId(),
                        AlertType.TEMP_OUT_OF_RANGE,
                        String.format("Temperature out of range: %s (%.1f °C)", robot.getName(), refrigerated.getCurrentTempC()),
                        refrigerated.getCurrentTempC()
                );
            }
        }

        if (telemetry.getStatus() != null) {
            robot.setStatus(telemetry.getStatus());
        }

        return toDto(robotRepository.save(robot));
    }

    @Transactional
    public RobotDto pauseRobot(UUID robotId) {
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new IllegalArgumentException("Robot not found with id: " + robotId));
        if (robot.getStatus() == RobotStatus.EN_ROUTE) {
            robot.setStatus(RobotStatus.PAUSED);
            robot = robotRepository.save(robot);
        }
        return toDto(robot);
    }

    @Transactional
    public RobotDto resumeRobot(UUID robotId) {
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new IllegalArgumentException("Robot not found with id: " + robotId));
        if (robot.getStatus() == RobotStatus.PAUSED) {
            robot.setStatus(RobotStatus.EN_ROUTE);
            robot = robotRepository.save(robot);
        }
        return toDto(robot);
    }

    public List<RobotDto> getFleetRobots(UUID fleetId, String type, RobotStatus status, RobotLifecycle lifecycle) {
        List<Robot> list = robotRepository.findByFleetId(fleetId);
        return list.stream()
                .filter(r -> type == null || (r instanceof Refrigerated ? "REFRIGERATED".equalsIgnoreCase(type) : "STANDARD".equalsIgnoreCase(type)))
                .filter(r -> status == null || r.getStatus() == status)
                .filter(r -> lifecycle == null || r.getLifecycle() == lifecycle)
                .map(this::toDto)
                .toList();
    }

    public RobotDto getRobotById(UUID robotId, UUID fleetId) {
        Robot robot = robotRepository.findByIdAndFleetId(robotId, fleetId)
                .orElseThrow(() -> new IllegalArgumentException("Robot not found"));
        return toDto(robot);
    }

    private void updateFleetRobotCounts(Fleet fleet) {
        if (fleet == null) return;
        List<Robot> robots = robotRepository.findByFleetId(fleet.getId());
        fleet.setNoOfRobots(robots.size());
        long activeCount = robots.stream().filter(r -> r.getLifecycle() == RobotLifecycle.ACTIVE).count();
        fleet.setNoOfActiveRobots((int) activeCount);
        fleetRepository.save(fleet);
    }

    public RobotDto toDto(Robot robot) {
        LocationDto locDto = null;
        if (robot.getLocation() != null) {
            locDto = new LocationDto(robot.getLocation().getLatitude(), robot.getLocation().getLongitude());
        }

        RobotDto.RobotDtoBuilder builder = RobotDto.builder()
                .id(robot.getId())
                .name(robot.getName())
                .status(robot.getStatus())
                .lifecycle(robot.getLifecycle())
                .battery(robot.getBattery())
                .mileageKm(robot.getMileageKm())
                .maxSpeedKm(robot.getMaxSpeedKm())
                .maxWeight(robot.getMaxWeight())
                .curWeight(robot.getCurWeight())
                .cargoSpaceLiters(robot.getCargoSpaceLiters())
                .location(locDto)
                .fleetId(robot.getFleet() != null ? robot.getFleet().getId() : null)
                .currentParcelId(robot.getCurrentParcel() != null ? robot.getCurrentParcel().getId() : null);

        if (robot instanceof Refrigerated refrig) {
            builder.robotType("REFRIGERATED")
                    .minTempC(refrig.getMinTempC())
                    .maxTempC(refrig.getMaxTempC())
                    .targetTempC(refrig.getTargetTempC())
                    .currentTempC(refrig.getCurrentTempC());
        } else {
            builder.robotType("STANDARD");
        }

        return builder.build();
    }
}
