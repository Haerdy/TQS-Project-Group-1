package com.tahelaev.tahelaev.dto;

import com.tahelaev.tahelaev.enums.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParcelCreateRequestDto {

    @NotNull(message = "End user ID is required")
    private UUID endUserId;

    @NotNull(message = "Fleet ID is required")
    private UUID fleetId;

    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;

    @NotBlank(message = "Destination is required")
    private String deliveryAddress;

    @Builder.Default
    private Size size = Size.M;

    private boolean isUrgent;
    private LocalDateTime pickupDeadline;
    private float distanceKm;

    @NotEmpty(message = "Add at least one medicine")
    private List<ItemCreateDto> items;
}
