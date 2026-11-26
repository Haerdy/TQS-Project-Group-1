package com.tahelaev.tahelaev.controller;

import com.tahelaev.tahelaev.dto.RobotCreateRequestDto;
import com.tahelaev.tahelaev.dto.RobotDto;
import com.tahelaev.tahelaev.dto.RobotLifecycleUpdateDto;
import com.tahelaev.tahelaev.dto.RobotTelemetryDto;
import com.tahelaev.tahelaev.enums.RobotLifecycle;
import com.tahelaev.tahelaev.enums.RobotStatus;
import com.tahelaev.tahelaev.service.RobotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/robots")
@RequiredArgsConstructor
public class RobotController {

    private final RobotService robotService;

    @PostMapping
    public ResponseEntity<RobotDto> registerRobot(@Valid @RequestBody RobotCreateRequestDto request) {
        RobotDto robot = robotService.registerRobot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(robot);
    }

    @GetMapping("/fleet/{fleetId}")
    public ResponseEntity<List<RobotDto>> getFleetRobots(
            @PathVariable UUID fleetId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) RobotStatus status,
            @RequestParam(required = false) RobotLifecycle lifecycle) {
        return ResponseEntity.ok(robotService.getFleetRobots(fleetId, type, status, lifecycle));
    }

    @GetMapping("/fleet/{fleetId}/{robotId}")
    public ResponseEntity<RobotDto> getRobotById(@PathVariable UUID fleetId, @PathVariable UUID robotId) {
        return ResponseEntity.ok(robotService.getRobotById(robotId, fleetId));
    }

    @PatchMapping("/{id}/lifecycle")
    public ResponseEntity<RobotDto> updateLifecycle(
            @PathVariable UUID id,
            @Valid @RequestBody RobotLifecycleUpdateDto request) {
        return ResponseEntity.ok(robotService.updateLifecycle(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRobot(@PathVariable UUID id) {
        robotService.deleteRobot(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/telemetry")
    public ResponseEntity<RobotDto> updateTelemetry(
            @PathVariable UUID id,
            @RequestBody RobotTelemetryDto telemetry) {
        return ResponseEntity.ok(robotService.updateTelemetry(id, telemetry));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<RobotDto> pauseRobot(@PathVariable UUID id) {
        return ResponseEntity.ok(robotService.pauseRobot(id));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<RobotDto> resumeRobot(@PathVariable UUID id) {
        return ResponseEntity.ok(robotService.resumeRobot(id));
    }
}
