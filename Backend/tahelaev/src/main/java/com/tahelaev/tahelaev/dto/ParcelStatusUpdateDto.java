package com.tahelaev.tahelaev.dto;

import com.tahelaev.tahelaev.enums.ParcelStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParcelStatusUpdateDto {
    @NotNull(message = "Status is required")
    private ParcelStatus status;
}
