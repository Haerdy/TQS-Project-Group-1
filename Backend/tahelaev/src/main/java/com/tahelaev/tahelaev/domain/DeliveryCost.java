package com.tahelaev.tahelaev.domain;

import jakarta.persistence.Column;
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

    @Column(name = "distance_km")
    private float distanceKm;

    @Column(name = "applied_km_rate")
    private float appliedKmRate;

    @Column(name = "parcel_items_total")
    private float parcelItemsTotal;

    @Column(name = "transport_cost")
    private float transportCost;

    @Column(name = "hospital_percent")
    private float hospitalPercent;

    @Column(name = "hospital_fee_amount")
    private float hospitalFeeAmount;

    @Column(name = "platform_percent")
    private float platformPercent;

    @Column(name = "platform_fee_amount")
    private float platformFeeAmount;

    @Column(name = "total_amount")
    private float totalAmount;
}
