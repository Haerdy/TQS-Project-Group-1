package com.tahelaev.tahelaev.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "fleets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fleet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "fleet_name", nullable = false, unique = true)
    private String fleetName;

    @Column(name = "no_of_robots", nullable = false)
    @Builder.Default
    private int noOfRobots = 0;

    @Column(name = "no_of_active_robots", nullable = false)
    @Builder.Default
    private int noOfActiveRobots = 0;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "system_admin_id")
    private SystemAdmin systemAdmin;

    @ManyToOne
    @JoinColumn(name = "platform_manager_id")
    private PlatformManager platformManager;

    @OneToOne(mappedBy = "fleet", cascade = CascadeType.ALL)
    private InstancePricePolicy instancePricePolicy;

    @OneToMany(mappedBy = "fleet", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Robot> robots = new ArrayList<>();

    @OneToMany(mappedBy = "fleet", cascade = CascadeType.ALL)
    @Builder.Default
    private List<EndUser> endUsers = new ArrayList<>();

    @OneToMany(mappedBy = "fleet", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Parcel> parcels = new ArrayList<>();

    @OneToMany(mappedBy = "fleet", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Alert> alerts = new ArrayList<>();
}
