package com.tahelaev.tahelaev.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "refrigerated_robots")
@DiscriminatorValue("REFRIGERATED")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Refrigerated extends Robot {

    @Column
    private Float minTempC;

    @Column
    private Float maxTempC;

    @Column
    private Float targetTempC;

    @Column
    private Float currentTempC;
}
