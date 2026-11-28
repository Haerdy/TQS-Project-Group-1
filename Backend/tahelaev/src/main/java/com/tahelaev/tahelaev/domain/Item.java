package com.tahelaev.tahelaev.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.util.UUID;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "value", nullable = false)
    private float value;

    @Column(name = "iva", nullable = false)
    private float iva;

    @Column(name = "weight", nullable = false)
    private float weight;

    @Column(name = "is_temp_sensitive", nullable = false)
    private boolean isTempSensitive;

    @Column(name = "temp")
    private Float temp;

    @ManyToOne
    @JoinColumn(name = "parcel_id")
    private Parcel parcel;
}
