package com.tahelaev.tahelaev.domain;

import jakarta.persistence.CascadeType;
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
@Table(name = "platform_managers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PlatformManager extends User {

    @OneToOne(mappedBy = "platformManager", cascade = CascadeType.ALL)
    private PlatformPricePolicy platformPricePolicy;

    @OneToMany(mappedBy = "platformManager")
    @Builder.Default
    private List<Fleet> fleets = new ArrayList<>();
}
