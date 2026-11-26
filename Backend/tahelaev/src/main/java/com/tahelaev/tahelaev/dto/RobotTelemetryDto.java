package com.tahelaev.tahelaev.dto;

import com.tahelaev.tahelaev.enums.RobotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RobotTelemetryDto {
    private LocationDto location;
    private Float battery;
    private Float mileageDeltaKm;
    private Float currentTempC;
    private RobotStatus status;
}
