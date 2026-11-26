package com.tahelaev.tahelaev.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryCostDto {
    private float distanceKm;
    private float appliedKmRate;
    private float parcelItemsTotal;
    private float transportCost;
    private float hospitalPercent;
    private float hospitalFeeAmount;
    private float platformPercent;
    private float platformFeeAmount;
    private float totalAmount;
}
