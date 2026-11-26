package com.tahelaev.tahelaev.service;

import com.tahelaev.tahelaev.domain.Fleet;
import com.tahelaev.tahelaev.domain.Parcel;
import com.tahelaev.tahelaev.domain.Refrigerated;
import com.tahelaev.tahelaev.domain.Robot;
import com.tahelaev.tahelaev.dto.ParcelDto;
import com.tahelaev.tahelaev.enums.AlertType;
import com.tahelaev.tahelaev.enums.ParcelStatus;
import com.tahelaev.tahelaev.enums.RobotLifecycle;
import com.tahelaev.tahelaev.enums.RobotStatus;
import com.tahelaev.tahelaev.repository.ParcelRepository;
import com.tahelaev.tahelaev.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DispatchService {

    private final RobotRepository robotRepository;
    private final ParcelRepository parcelRepository;
    private final AlertService alertService;

    public static final float MIN_BATTERY_TO_BOOK = 20.0f; // BR1
    public static final float BATTERY_DRAIN_PER_KM = 5.0f;  // BR2
    public static final float MIN_BATTERY_RESERVE = 10.0f;  // BR2

    @Transactional
    public boolean dispatchOrder(Parcel parcel) {
        Fleet fleet = parcel.getFleet();
        if (fleet == null) return false;

        List<Robot> activeRobots = robotRepository.findByFleetIdAndLifecycleAndStatus(
                fleet.getId(), RobotLifecycle.ACTIVE, RobotStatus.IDLE
        );

        float distanceKm = parcel.getDeliveryCost() != null ? parcel.getDeliveryCost().getDistanceKm() : 1.0f;
        float roundTripKm = distanceKm * 2.0f;

        // Filter eligible robots based on BR1 - BR4
        List<Robot> eligible = activeRobots.stream()
                .filter(r -> r.getBattery() >= MIN_BATTERY_TO_BOOK) // BR1
                .filter(r -> (r.getBattery() - (roundTripKm * BATTERY_DRAIN_PER_KM)) >= MIN_BATTERY_RESERVE) // BR2
                .filter(r -> r.getMaxWeight() >= parcel.getWeight()) // BR3
                .filter(r -> !parcel.isNeedsRefrigeration() || r instanceof Refrigerated) // BR4
                .sorted(Comparator.comparing(Robot::getBattery).reversed()) // Tie-breaker: higher battery
                .toList();

        if (eligible.isEmpty()) {
            alertService.createAlert(
                    fleet.getId(),
                    null,
                    AlertType.NO_ROBOTS_AVAILABLE,
                    "No robots available for order: " + parcel.getTrackingCode(),
                    0.0f
            );
            return false;
        }

        Robot bestRobot = eligible.get(0);

        parcel.setStatus(ParcelStatus.ASSIGNED);
        parcel.setRobot(bestRobot);
        parcelRepository.save(parcel);

        bestRobot.setStatus(RobotStatus.EN_ROUTE);
        bestRobot.setCurrentParcel(parcel);
        robotRepository.save(bestRobot);

        return true;
    }

    @Transactional
    public void processWaitingOrders(UUID fleetId) {
        // BR11: Urgent before Normal, then oldest first
        List<Parcel> waitingOrders = parcelRepository.findByFleetIdAndStatusOrderByIsUrgentDescCreatedAtAsc(
                fleetId, ParcelStatus.CREATED
        );

        for (Parcel parcel : waitingOrders) {
            boolean assigned = dispatchOrder(parcel);
            if (!assigned) {
                break; // No robot available for the current queue head
            }
        }
    }

    @Transactional
    public void handleRobotMalfunction(UUID robotId, String reason) {
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new IllegalArgumentException("Robot not found with id: " + robotId));

        robot.setStatus(RobotStatus.OFFLINE);
        Parcel parcel = robot.getCurrentParcel();

        alertService.createAlert(
                robot.getFleet().getId(),
                robot.getId(),
                AlertType.ROBOT_FAILURE,
                "Robot failure: " + robot.getName() + (reason != null ? " - " + reason : ""),
                robot.getBattery()
        );

        if (parcel != null) {
            // BR12: If assigned (before pickup), requeue order; if in transit, mark failed
            if (parcel.getStatus() == ParcelStatus.ASSIGNED) {
                parcel.setStatus(ParcelStatus.CREATED);
                parcel.setRobot(null);
                parcelRepository.save(parcel);
                // Try to assign next eligible robot immediately
                dispatchOrder(parcel);
            } else if (parcel.getStatus() == ParcelStatus.IN_TRANSIT) {
                parcel.setStatus(ParcelStatus.FAILED);
                parcelRepository.save(parcel);
            }
            robot.setCurrentParcel(null);
        }

        robotRepository.save(robot);
    }

    @Transactional
    public void recallRobot(UUID robotId, String reason) {
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new IllegalArgumentException("Robot not found with id: " + robotId));

        Parcel parcel = robot.getCurrentParcel();
        if (parcel != null) {
            // BR12: If assigned, requeue; if in transit, fail with reason
            if (parcel.getStatus() == ParcelStatus.ASSIGNED) {
                parcel.setStatus(ParcelStatus.CREATED);
                parcel.setRobot(null);
                parcelRepository.save(parcel);
            } else if (parcel.getStatus() == ParcelStatus.IN_TRANSIT) {
                parcel.setStatus(ParcelStatus.FAILED);
                parcelRepository.save(parcel);
            }
            robot.setCurrentParcel(null);
        }

        robot.setStatus(RobotStatus.IDLE);
        robotRepository.save(robot);
    }

    @Transactional
    public void manualReassign(Long parcelId, UUID newRobotId, String note) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found with id: " + parcelId));

        if (parcel.getStatus() == ParcelStatus.IN_TRANSIT) {
            throw new IllegalStateException("The medicines are already on board");
        }

        Robot newRobot = robotRepository.findById(newRobotId)
                .orElseThrow(() -> new IllegalArgumentException("Robot not found with id: " + newRobotId));

        if (newRobot.getBattery() < MIN_BATTERY_TO_BOOK) {
            throw new IllegalArgumentException(newRobot.getName() + " is below the minimum battery level");
        }

        Robot oldRobot = parcel.getRobot();
        if (oldRobot != null) {
            oldRobot.setCurrentParcel(null);
            oldRobot.setStatus(RobotStatus.IDLE);
            robotRepository.save(oldRobot);
        }

        parcel.setRobot(newRobot);
        parcel.setStatus(ParcelStatus.ASSIGNED);
        parcelRepository.save(parcel);

        newRobot.setCurrentParcel(parcel);
        newRobot.setStatus(RobotStatus.EN_ROUTE);
        robotRepository.save(newRobot);
    }
}
