package com.tahelaev.tahelaev.repository;

import com.tahelaev.tahelaev.domain.InstancePricePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstancePricePolicyRepository extends JpaRepository<InstancePricePolicy, UUID> {
    Optional<InstancePricePolicy> findByFleetId(UUID fleetId);
}
