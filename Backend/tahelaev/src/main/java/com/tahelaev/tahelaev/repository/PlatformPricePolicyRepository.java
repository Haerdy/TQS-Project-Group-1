package com.tahelaev.tahelaev.repository;

import com.tahelaev.tahelaev.domain.PlatformPricePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformPricePolicyRepository extends JpaRepository<PlatformPricePolicy, UUID> {
    Optional<PlatformPricePolicy> findByPlatformManagerId(UUID platformManagerId);
}
