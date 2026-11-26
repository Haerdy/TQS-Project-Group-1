package com.tahelaev.tahelaev.domain;

import jakarta.persistence.Entity;
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

@Entity
@Table(name = "system_admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SystemAdmin extends User {

    @OneToOne(mappedBy = "systemAdmin")
    private Fleet fleet;

    @OneToMany(mappedBy = "acknowledgedBy")
    @Builder.Default
    private List<Alert> acknowledgedAlerts = new ArrayList<>();
}
