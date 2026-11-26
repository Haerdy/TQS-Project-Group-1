package com.tahelaev.tahelaev.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RobotCreateRequestDto {

    @NotBlank(message = "Robot name is required")
    private String name;

    @NotNull(message = "Fleet ID is required")
    private UUID fleetId;

    @NotBlank(message = "Robot type is required (STANDARD or REFRIGERATED)")
    private String robotType;

    @Positive(message = "Payload capacity must be greater than 0")
    private Float maxWeight;

    @Builder.Default
    private Float maxSpeedKm = 20.0f;

    @Builder.Default
    private Double cargoSpaceLiters = 50.0;

    // Optional temperature bounds for refrigerated robots
    private Float minTempC;
    private Float maxTempC;
    private Float targetTempC;
}
