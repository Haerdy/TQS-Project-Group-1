package com.tahelaev.tahelaev.controller;

import com.tahelaev.tahelaev.dto.ParcelCreateRequestDto;
import com.tahelaev.tahelaev.dto.ParcelDto;
import com.tahelaev.tahelaev.dto.ParcelStatusUpdateDto;
import com.tahelaev.tahelaev.service.ParcelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parcels")
@RequiredArgsConstructor
public class ParcelController {

    private final ParcelService parcelService;

    @PostMapping
    public ResponseEntity<ParcelDto> createParcel(@Valid @RequestBody ParcelCreateRequestDto request) {
        ParcelDto parcel = parcelService.createParcel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(parcel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParcelDto> getParcelById(@PathVariable Long id) {
        return ResponseEntity.ok(parcelService.getParcelById(id));
    }

    @GetMapping("/track/{trackingCode}")
    public ResponseEntity<ParcelDto> getParcelByTrackingCode(@PathVariable String trackingCode) {
        return ResponseEntity.ok(parcelService.getParcelByTrackingCode(trackingCode));
    }

    @GetMapping("/fleet/{fleetId}")
    public ResponseEntity<List<ParcelDto>> getParcelsByFleet(@PathVariable UUID fleetId) {
        return ResponseEntity.ok(parcelService.getParcelsByFleet(fleetId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ParcelDto>> getParcelsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(parcelService.getParcelsByEndUser(userId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ParcelDto> cancelParcel(@PathVariable Long id) {
        return ResponseEntity.ok(parcelService.cancelParcel(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ParcelDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ParcelStatusUpdateDto request) {
        return ResponseEntity.ok(parcelService.updateStatus(id, request));
    }
}
