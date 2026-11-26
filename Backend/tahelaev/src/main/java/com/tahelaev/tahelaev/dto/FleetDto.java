package com.tahelaev.tahelaev.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FleetDto {
    private UUID id;
    private String fleetName;
    private int noOfRobots;
    private int noOfActiveRobots;
    private UUID systemAdminId;
    private String systemAdminEmail;
    private UUID platformManagerId;
    private InstancePricePolicyDto instancePricePolicy;
}
