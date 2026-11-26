package com.tahelaev.tahelaev.domain;

import com.tahelaev.tahelaev.enums.AlertType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private float value;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime time = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private boolean acknowledged = false;

    @Column
    private LocalDateTime ackTime;

    @ManyToOne
    @JoinColumn(name = "fleet_id", nullable = false)
    private Fleet fleet;

    @ManyToOne
    @JoinColumn(name = "robot_id")
    private Robot robot;

    @ManyToOne
    @JoinColumn(name = "acknowledged_by_id")
    private SystemAdmin acknowledgedBy;
}
