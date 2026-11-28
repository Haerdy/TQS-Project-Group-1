package com.tahelaev.tahelaev.domain;

import com.tahelaev.tahelaev.enums.RobotLifecycle;
import com.tahelaev.tahelaev.enums.RobotStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
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
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "robots")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "robot_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Robot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private RobotStatus status = RobotStatus.OFFLINE;

    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle", nullable = false)
    @Builder.Default
    private RobotLifecycle lifecycle = RobotLifecycle.REGISTERED;

    @Column(name = "battery", nullable = false)
    @Builder.Default
    private Float battery = 100.0f;

    @Column(name = "mileage_km", nullable = false)
    @Builder.Default
    private Float mileageKm = 0.0f;

    @Column(name = "max_speed_km", nullable = false)
    private Float maxSpeedKm;

    @Column(name = "max_weight", nullable = false)
    private Float maxWeight;

    @Column(name = "cur_weight", nullable = false)
    @Builder.Default
    private Float curWeight = 0.0f;

    @Column(name = "cargo_space_liters", nullable = false)
    private Double cargoSpaceLiters;

    @Embedded
    private Location location;

    @ManyToOne
    @JoinColumn(name = "fleet_id")
    private Fleet fleet;

    @OneToOne
    @JoinColumn(name = "current_parcel_id")
    private Parcel currentParcel;

    @OneToMany(mappedBy = "robot", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Alert> alerts = new ArrayList<>();
}
