package com.tahelaev.tahelaev.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformPricePolicyDto {
    private UUID id;

    @PositiveOrZero(message = "Robot monthly fee must be positive or zero")
    private float robotMonthlyFee;

    @PositiveOrZero(message = "Refrigerated robot monthly fee must be positive or zero")
    private float refrigMonthlyFee;

    @DecimalMin(value = "0.0", message = "Mileage percent fee must be >= 0")
    @DecimalMax(value = "100.0", message = "Mileage percent fee must be <= 100")
    private float mileagePercentFee;

    @DecimalMin(value = "0.0", message = "Parcel percent fee must be >= 0")
    @DecimalMax(value = "100.0", message = "Parcel percent fee must be <= 100")
    private float parcelPercentFee;

    private UUID platformManagerId;
}
