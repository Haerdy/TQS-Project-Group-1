package com.tahelaev.tahelaev.controller;

import com.tahelaev.tahelaev.dto.FleetCreateRequestDto;
import com.tahelaev.tahelaev.dto.FleetDto;
import com.tahelaev.tahelaev.dto.InstancePricePolicyDto;
import com.tahelaev.tahelaev.service.FleetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fleets")
@RequiredArgsConstructor
public class FleetController {

    private final FleetService fleetService;

    @PostMapping
    public ResponseEntity<FleetDto> createFleet(@Valid @RequestBody FleetCreateRequestDto request) {
        FleetDto fleet = fleetService.createFleet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(fleet);
    }

    @GetMapping
    public ResponseEntity<List<FleetDto>> getAllFleets() {
        return ResponseEntity.ok(fleetService.getAllFleets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FleetDto> getFleetById(@PathVariable UUID id) {
        return ResponseEntity.ok(fleetService.getFleetById(id));
    }

    @PutMapping("/{id}/pricing-policy")
    public ResponseEntity<InstancePricePolicyDto> updatePricingPolicy(
            @PathVariable UUID id,
            @Valid @RequestBody InstancePricePolicyDto dto) {
        return ResponseEntity.ok(fleetService.updateInstancePricePolicy(id, dto));
    }
}
