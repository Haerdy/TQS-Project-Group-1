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
public class MonthlyRevenueReportDto {
    private UUID fleetId;
    private String fleetName;
    private int year;
    private int month;
    private int robotsUsed;
    private float robotFees;
    private float platformPercentageRevenue;
    private float totalAmount;
}
