package com.tahelaev.tahelaev.domain;

import com.tahelaev.tahelaev.enums.ParcelStatus;
import com.tahelaev.tahelaev.enums.Size;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parcels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parcel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tracking_code", nullable = false, unique = true)
    private String trackingCode;

    @Column(name = "pickup_code", nullable = false)
    private String pickupCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "size", nullable = false)
    private Size size;

    @Column(name = "weight", nullable = false)
    private float weight;

    @Column(name = "needs_refrigeration", nullable = false)
    @Builder.Default
    private boolean needsRefrigeration = false;

    @Column(name = "storage_temperature")
    private Float storageTemperature;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ParcelStatus status = ParcelStatus.CREATED;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "pickup_deadline")
    private LocalDateTime pickupDeadline;

    @Column(name = "pickup_address", nullable = false)
    private String pickupAddress;

    @Column(name = "is_urgent", nullable = false)
    @Builder.Default
    private boolean isUrgent = false;

    @Embedded
    private DeliveryCost deliveryCost;

    @ManyToOne
    @JoinColumn(name = "end_user_id")
    private EndUser endUser;

    @OneToOne
    @JoinColumn(name = "robot_id")
    private Robot robot;

    @ManyToOne
    @JoinColumn(name = "fleet_id")
    private Fleet fleet;

    @OneToMany(mappedBy = "parcel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Item> items = new ArrayList<>();
}
