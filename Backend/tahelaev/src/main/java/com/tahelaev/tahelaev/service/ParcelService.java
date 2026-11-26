package com.tahelaev.tahelaev.service;

import com.tahelaev.tahelaev.domain.DeliveryCost;
import com.tahelaev.tahelaev.domain.EndUser;
import com.tahelaev.tahelaev.domain.Fleet;
import com.tahelaev.tahelaev.domain.Item;
import com.tahelaev.tahelaev.domain.Parcel;
import com.tahelaev.tahelaev.domain.Refrigerated;
import com.tahelaev.tahelaev.domain.Robot;
import com.tahelaev.tahelaev.dto.DeliveryCostDto;
import com.tahelaev.tahelaev.dto.ItemCreateDto;
import com.tahelaev.tahelaev.dto.ItemDto;
import com.tahelaev.tahelaev.dto.ParcelCreateRequestDto;
import com.tahelaev.tahelaev.dto.ParcelDto;
import com.tahelaev.tahelaev.dto.ParcelStatusUpdateDto;
import com.tahelaev.tahelaev.enums.ParcelStatus;
import com.tahelaev.tahelaev.enums.RobotLifecycle;
import com.tahelaev.tahelaev.enums.RobotStatus;
import com.tahelaev.tahelaev.repository.EndUserRepository;
import com.tahelaev.tahelaev.repository.FleetRepository;
import com.tahelaev.tahelaev.repository.ParcelRepository;
import com.tahelaev.tahelaev.repository.RefrigeratedRobotRepository;
import com.tahelaev.tahelaev.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParcelService {

    private final ParcelRepository parcelRepository;
    private final FleetRepository fleetRepository;
    private final EndUserRepository endUserRepository;
    private final RobotRepository robotRepository;
    private final RefrigeratedRobotRepository refrigeratedRobotRepository;
    private final PricingService pricingService;
    private final DispatchService dispatchService;

    @Transactional
    public ParcelDto createParcel(ParcelCreateRequestDto request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Add at least one medicine");
        }
        if (request.getDeliveryAddress() == null || request.getDeliveryAddress().isBlank()) {
            throw new IllegalArgumentException("Destination is required");
        }

        Fleet fleet = fleetRepository.findById(request.getFleetId())
                .orElseThrow(() -> new IllegalArgumentException("Fleet not found with id: " + request.getFleetId()));

        EndUser endUser = endUserRepository.findById(request.getEndUserId())
                .orElseThrow(() -> new IllegalArgumentException("End user not found with id: " + request.getEndUserId()));

        float totalWeight = (float) request.getItems().stream().mapToDouble(ItemCreateDto::getWeight).sum();
        boolean needsRefrigeration = request.getItems().stream().anyMatch(ItemCreateDto::isTempSensitive);

        // BR4: Check if hospital has active refrigerated robot
        if (needsRefrigeration) {
            List<Refrigerated> activeRefrigRobots = refrigeratedRobotRepository.findByFleetIdAndLifecycle(
                    fleet.getId(), RobotLifecycle.ACTIVE
            );
            if (activeRefrigRobots.isEmpty()) {
                throw new IllegalStateException("Refrigerated delivery is not available right now");
            }
        }

        // BR3: Order weight must fit the largest robot in the fleet
        List<Robot> activeRobots = robotRepository.findByFleetIdAndLifecycle(fleet.getId(), RobotLifecycle.ACTIVE);
        if (!activeRobots.isEmpty()) {
            float maxCapacity = (float) activeRobots.stream().mapToDouble(Robot::getMaxWeight).max().orElse(0.0);
            if (totalWeight > maxCapacity) {
                throw new IllegalArgumentException("This order is too heavy for a single robot");
            }
        }

        String trackingCode = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String pickupCode = String.format("%04d", (int) (Math.random() * 10000));

        List<Item> items = new ArrayList<>();
        Parcel parcel = Parcel.builder()
                .trackingCode(trackingCode)
                .pickupCode(pickupCode)
                .size(request.getSize())
                .weight(totalWeight)
                .needsRefrigeration(needsRefrigeration)
                .storageTemperature(needsRefrigeration ? 4.0f : null)
                .deliveryAddress(request.getDeliveryAddress())
                .status(ParcelStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .pickupDeadline(request.getPickupDeadline())
                .pickupAddress(request.getPickupAddress())
                .isUrgent(request.isUrgent())
                .fleet(fleet)
                .endUser(endUser)
                .build();

        for (ItemCreateDto itemDto : request.getItems()) {
            Item item = Item.builder()
                    .name(itemDto.getName())
                    .value(itemDto.getValue())
                    .iva(itemDto.getIva())
                    .weight(itemDto.getWeight())
                    .isTempSensitive(itemDto.isTempSensitive())
                    .temp(itemDto.getTemp())
                    .parcel(parcel)
                    .build();
            items.add(item);
        }
        parcel.setItems(items);

        // BR7: Freeze delivery cost on confirmation
        float distance = request.getDistanceKm() > 0 ? request.getDistanceKm() : 2.0f;
        DeliveryCost cost = pricingService.computeDeliveryCost(fleet, distance, items, needsRefrigeration);
        parcel.setDeliveryCost(cost);

        parcel = parcelRepository.save(parcel);

        // Try dispatching immediately
        dispatchService.dispatchOrder(parcel);

        return toDto(parcelRepository.findById(parcel.getId()).orElse(parcel));
    }

    @Transactional
    public ParcelDto cancelParcel(Long parcelId) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found with id: " + parcelId));

        ParcelStatus status = parcel.getStatus();

        // BR5: Cancellation rules
        if (status == ParcelStatus.IN_TRANSIT) {
            throw new IllegalStateException("Already picked up — contact the hospital");
        } else if (status == ParcelStatus.DELIVERED) {
            throw new IllegalStateException("Order already delivered");
        } else if (status == ParcelStatus.CANCELLED || status == ParcelStatus.FAILED) {
            throw new IllegalStateException("Order cannot be cancelled in status: " + status);
        }

        // If ASSIGNED, release robot
        if (status == ParcelStatus.ASSIGNED && parcel.getRobot() != null) {
            Robot robot = parcel.getRobot();
            robot.setCurrentParcel(null);
            robot.setStatus(RobotStatus.IDLE);
            robotRepository.save(robot);
            parcel.setRobot(null);
        }

        parcel.setStatus(ParcelStatus.CANCELLED);
        return toDto(parcelRepository.save(parcel));
    }

    @Transactional
    public ParcelDto updateStatus(Long parcelId, ParcelStatusUpdateDto request) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found with id: " + parcelId));

        parcel.setStatus(request.getStatus());

        if (request.getStatus() == ParcelStatus.DELIVERED) {
            if (parcel.getRobot() != null) {
                Robot robot = parcel.getRobot();
                robot.setCurrentParcel(null);
                robot.setStatus(RobotStatus.IDLE);
                robotRepository.save(robot);
            }
        }

        return toDto(parcelRepository.save(parcel));
    }

    public ParcelDto getParcelById(Long parcelId) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found with id: " + parcelId));
        return toDto(parcel);
    }

    public ParcelDto getParcelByTrackingCode(String trackingCode) {
        Parcel parcel = parcelRepository.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found with tracking code: " + trackingCode));
        return toDto(parcel);
    }

    public List<ParcelDto> getParcelsByFleet(UUID fleetId) {
        return parcelRepository.findByFleetId(fleetId).stream().map(this::toDto).toList();
    }

    public List<ParcelDto> getParcelsByEndUser(UUID endUserId) {
        return parcelRepository.findByEndUserId(endUserId).stream().map(this::toDto).toList();
    }

    public ParcelDto toDto(Parcel parcel) {
        DeliveryCostDto costDto = null;
        if (parcel.getDeliveryCost() != null) {
            DeliveryCost cost = parcel.getDeliveryCost();
            costDto = DeliveryCostDto.builder()
                    .distanceKm(cost.getDistanceKm())
                    .appliedKmRate(cost.getAppliedKmRate())
                    .parcelItemsTotal(cost.getParcelItemsTotal())
                    .transportCost(cost.getTransportCost())
                    .hospitalPercent(cost.getHospitalPercent())
                    .hospitalFeeAmount(cost.getHospitalFeeAmount())
                    .platformPercent(cost.getPlatformPercent())
                    .platformFeeAmount(cost.getPlatformFeeAmount())
                    .totalAmount(cost.getTotalAmount())
                    .build();
        }

        List<ItemDto> itemDtos = parcel.getItems() != null
                ? parcel.getItems().stream().map(i -> ItemDto.builder()
                        .id(i.getId())
                        .name(i.getName())
                        .value(i.getValue())
                        .iva(i.getIva())
                        .weight(i.getWeight())
                        .isTempSensitive(i.isTempSensitive())
                        .temp(i.getTemp())
                        .build()).toList()
                : List.of();

        return ParcelDto.builder()
                .id(parcel.getId())
                .trackingCode(parcel.getTrackingCode())
                .pickupCode(parcel.getPickupCode())
                .size(parcel.getSize())
                .weight(parcel.getWeight())
                .needsRefrigeration(parcel.isNeedsRefrigeration())
                .storageTemperature(parcel.getStorageTemperature())
                .deliveryAddress(parcel.getDeliveryAddress())
                .status(parcel.getStatus())
                .createdAt(parcel.getCreatedAt())
                .pickupDeadline(parcel.getPickupDeadline())
                .pickupAddress(parcel.getPickupAddress())
                .isUrgent(parcel.isUrgent())
                .deliveryCost(costDto)
                .endUserId(parcel.getEndUser() != null ? parcel.getEndUser().getId() : null)
                .robotId(parcel.getRobot() != null ? parcel.getRobot().getId() : null)
                .fleetId(parcel.getFleet() != null ? parcel.getFleet().getId() : null)
                .items(itemDtos)
                .build();
    }
}
