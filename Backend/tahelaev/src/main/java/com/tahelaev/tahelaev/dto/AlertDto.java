package com.tahelaev.tahelaev.dto;

import com.tahelaev.tahelaev.enums.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDto {
    private UUID id;
    private AlertType type;
    private String message;
    private float value;
    private LocalDateTime time;
    private boolean acknowledged;
    private LocalDateTime ackTime;
    private UUID fleetId;
    private UUID robotId;
    private UUID acknowledgedById;
}
