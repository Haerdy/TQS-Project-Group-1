package com.tahelaev.tahelaev.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "instance_price_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstancePricePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "base_km_fee", nullable = false)
    private float baseKmFee;

    @Column(name = "refrigerated_km_fee", nullable = false)
    private float refrigeratedKmFee;

    @Column(name = "parcel_percent_fee", nullable = false)
    private float parcelPercentFee;

    @OneToOne
    @JoinColumn(name = "fleet_id")
    private Fleet fleet;
}
