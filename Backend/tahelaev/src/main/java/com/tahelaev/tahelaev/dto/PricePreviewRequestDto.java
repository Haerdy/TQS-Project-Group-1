package com.tahelaev.tahelaev.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricePreviewRequestDto {

    @NotNull(message = "Fleet ID is required")
    private UUID fleetId;

    @Positive(message = "Distance must be positive")
    private float distanceKm;

    @NotEmpty(message = "Add at least one medicine")
    private List<ItemCreateDto> items;
}
