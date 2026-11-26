package com.tahelaev.tahelaev.dto;

import com.tahelaev.tahelaev.enums.RobotLifecycle;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RobotLifecycleUpdateDto {
    @NotNull(message = "Lifecycle state is required")
    private RobotLifecycle lifecycle;
}
