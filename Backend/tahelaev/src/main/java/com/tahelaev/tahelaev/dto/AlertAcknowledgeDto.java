package com.tahelaev.tahelaev.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertAcknowledgeDto {
    @NotNull(message = "System admin ID is required")
    private UUID systemAdminId;
}
