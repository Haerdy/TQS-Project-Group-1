package com.tahelaev.tahelaev.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryCost {

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
