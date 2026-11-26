package com.tahelaev.tahelaev.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FleetCreateRequestDto {

    @NotBlank(message = "Fleet name is required")
    private String fleetName;

    @NotBlank(message = "System admin name is required")
    private String systemAdminName;

    @NotBlank(message = "System admin email is required")
    @Email(message = "System admin email must be valid")
    private String systemAdminEmail;

    @NotBlank(message = "System admin password is required")
    private String systemAdminPassword;

    private UUID platformManagerId;
}
