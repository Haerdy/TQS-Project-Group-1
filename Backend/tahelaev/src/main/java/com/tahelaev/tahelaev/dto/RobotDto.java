package com.tahelaev.tahelaev.dto;

import com.tahelaev.tahelaev.enums.RobotLifecycle;
import com.tahelaev.tahelaev.enums.RobotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RobotDto {
    private UUID id;
    private String name;
    private String robotType;
    private RobotStatus status;
    private RobotLifecycle lifecycle;
    private Float battery;
    private Float mileageKm;
    private Float maxSpeedKm;
    private Float maxWeight;
    private Float curWeight;
    private Double cargoSpaceLiters;
    private LocationDto location;
    private UUID fleetId;
    private Long currentParcelId;

    // Refrigerated attributes
    private Float minTempC;
    private Float maxTempC;
    private Float targetTempC;
    private Float currentTempC;
}
