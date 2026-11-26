package com.tahelaev.tahelaev.repository;

import com.tahelaev.tahelaev.domain.Parcel;
import com.tahelaev.tahelaev.enums.ParcelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Long> {
    Optional<Parcel> findByTrackingCode(String trackingCode);
    List<Parcel> findByFleetId(UUID fleetId);
    List<Parcel> findByEndUserId(UUID endUserId);
    List<Parcel> findByFleetIdAndStatus(UUID fleetId, ParcelStatus status);
    List<Parcel> findByFleetIdAndStatusOrderByIsUrgentDescCreatedAtAsc(UUID fleetId, ParcelStatus status);
    List<Parcel> findByRobotId(UUID robotId);
}
