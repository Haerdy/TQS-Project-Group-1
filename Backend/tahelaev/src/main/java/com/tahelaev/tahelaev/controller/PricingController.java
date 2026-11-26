package com.tahelaev.tahelaev.controller;

import com.tahelaev.tahelaev.dto.MonthlyRevenueReportDto;
import com.tahelaev.tahelaev.dto.PricePreviewRequestDto;
import com.tahelaev.tahelaev.dto.PricePreviewResponseDto;
import com.tahelaev.tahelaev.service.PricingService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    @PostMapping("/preview")
    public ResponseEntity<PricePreviewResponseDto> getPricePreview(@Valid @RequestBody PricePreviewRequestDto request) {
        return ResponseEntity.ok(pricingService.calculatePricePreview(request));
    }

    @GetMapping("/reports/{fleetId}")
    public ResponseEntity<MonthlyRevenueReportDto> getMonthlyReport(
            @PathVariable UUID fleetId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(pricingService.generateMonthlyReport(fleetId, year, month));
    }
}
