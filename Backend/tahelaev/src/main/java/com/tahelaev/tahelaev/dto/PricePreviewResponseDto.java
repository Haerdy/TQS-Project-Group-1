package com.tahelaev.tahelaev.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricePreviewResponseDto {
    private float medicinesTotal;
    private float distanceKm;
    private float transportCost;
    private float appliedKmRate;
    private boolean requiresRefrigeration;
    private float hospitalFee;
    private float platformFee;
    private float total;
}
