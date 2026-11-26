package com.tahelaev.tahelaev.dto;

import com.tahelaev.tahelaev.enums.ParcelStatus;
import com.tahelaev.tahelaev.enums.Size;
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
public class ParcelDto {
    private Long id;
    private String trackingCode;
    private String pickupCode;
    private Size size;
    private float weight;
    private boolean needsRefrigeration;
    private Float storageTemperature;
    private String deliveryAddress;
    private ParcelStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime pickupDeadline;
    private String pickupAddress;
    private boolean isUrgent;
    private DeliveryCostDto deliveryCost;
    private UUID endUserId;
    private UUID robotId;
    private UUID fleetId;
    private List<ItemDto> items;
}
