package com.tahelaev.tahelaev.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "end_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EndUser extends User {

    @Column
    private String address;

    @ManyToOne
    @JoinColumn(name = "fleet_id")
    private Fleet fleet;

    @OneToMany(mappedBy = "endUser")
    @Builder.Default
    private List<Parcel> parcels = new ArrayList<>();
}
