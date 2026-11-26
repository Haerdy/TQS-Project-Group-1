package com.tahelaev.tahelaev.controller;

import com.tahelaev.tahelaev.dto.AlertAcknowledgeDto;
import com.tahelaev.tahelaev.dto.AlertDto;
import com.tahelaev.tahelaev.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/fleet/{fleetId}")
    public ResponseEntity<List<AlertDto>> getFleetAlerts(
            @PathVariable UUID fleetId,
            @RequestParam(required = false) Boolean unacknowledgedOnly) {
        return ResponseEntity.ok(alertService.getAlertsByFleet(fleetId, unacknowledgedOnly));
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<AlertDto> acknowledgeAlert(
            @PathVariable UUID id,
            @Valid @RequestBody AlertAcknowledgeDto request) {
        return ResponseEntity.ok(alertService.acknowledgeAlert(id, request.getSystemAdminId()));
    }
}
