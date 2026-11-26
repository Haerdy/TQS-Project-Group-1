package com.tahelaev.tahelaev.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "standard_robots")
@DiscriminatorValue("STANDARD")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Standard extends Robot {
}
