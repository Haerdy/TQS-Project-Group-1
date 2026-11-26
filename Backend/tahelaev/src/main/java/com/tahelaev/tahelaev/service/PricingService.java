package com.tahelaev.tahelaev.service;

import com.tahelaev.tahelaev.domain.DeliveryCost;
import com.tahelaev.tahelaev.domain.Fleet;
import com.tahelaev.tahelaev.domain.InstancePricePolicy;
import com.tahelaev.tahelaev.domain.Item;
import com.tahelaev.tahelaev.domain.Parcel;
import com.tahelaev.tahelaev.domain.PlatformPricePolicy;
import com.tahelaev.tahelaev.dto.ItemCreateDto;
import com.tahelaev.tahelaev.dto.MonthlyRevenueReportDto;
import com.tahelaev.tahelaev.dto.PricePreviewRequestDto;
import com.tahelaev.tahelaev.dto.PricePreviewResponseDto;
import com.tahelaev.tahelaev.enums.ParcelStatus;
import com.tahelaev.tahelaev.repository.FleetRepository;
import com.tahelaev.tahelaev.repository.InstancePricePolicyRepository;
import com.tahelaev.tahelaev.repository.ParcelRepository;
import com.tahelaev.tahelaev.repository.PlatformPricePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final FleetRepository fleetRepository;
    private final InstancePricePolicyRepository instancePricePolicyRepository;
    private final PlatformPricePolicyRepository platformPricePolicyRepository;
    private final ParcelRepository parcelRepository;

    // Default fallbacks if policies are not set
    public static final float DEFAULT_BASE_KM_FEE = 1.50f;
    public static final float DEFAULT_REFRIGERATED_KM_FEE = 2.00f;
    public static final float DEFAULT_HOSPITAL_PERCENT = 10.0f;
    public static final float DEFAULT_PLATFORM_PERCENT = 10.0f;
    public static final float DEFAULT_ROBOT_MONTHLY_FEE = 50.0f;

    private float roundHalfUp(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).floatValue();
    }

    public PricePreviewResponseDto calculatePricePreview(PricePreviewRequestDto request) {
        Fleet fleet = fleetRepository.findById(request.getFleetId())
                .orElseThrow(() -> new IllegalArgumentException("Fleet not found with id: " + request.getFleetId()));

        boolean requiresRefrigeration = request.getItems().stream().anyMatch(ItemCreateDto::isTempSensitive);

        float parcelValue = (float) request.getItems().stream()
                .mapToDouble(ItemCreateDto::getValue)
                .sum();
        parcelValue = roundHalfUp(parcelValue);

        float rate = getApplicableKmRate(fleet, requiresRefrigeration);
        float hospitalPercent = getHospitalPercentFee(fleet);
        float platformPercent = getPlatformPercentFee(fleet);

        float transportCost = roundHalfUp(request.getDistanceKm() * rate);
        float hospitalFee = roundHalfUp(parcelValue * (hospitalPercent / 100.0f));
        float platformFee = roundHalfUp((parcelValue + transportCost) * (platformPercent / 100.0f));
        float total = roundHalfUp(parcelValue + transportCost + hospitalFee + platformFee);

        return PricePreviewResponseDto.builder()
                .medicinesTotal(parcelValue)
                .distanceKm(request.getDistanceKm())
                .appliedKmRate(rate)
                .transportCost(transportCost)
                .requiresRefrigeration(requiresRefrigeration)
                .hospitalFee(hospitalFee)
                .platformFee(platformFee)
                .total(total)
                .build();
    }

    public DeliveryCost computeDeliveryCost(Fleet fleet, float distanceKm, List<Item> items, boolean needsRefrigeration) {
        float parcelValue = (float) items.stream().mapToDouble(Item::getValue).sum();
        parcelValue = roundHalfUp(parcelValue);

        float rate = getApplicableKmRate(fleet, needsRefrigeration);
        float hospitalPercent = getHospitalPercentFee(fleet);
        float platformPercent = getPlatformPercentFee(fleet);

        float transportCost = roundHalfUp(distanceKm * rate);
        float hospitalFeeAmount = roundHalfUp(parcelValue * (hospitalPercent / 100.0f));
        float platformFeeAmount = roundHalfUp((parcelValue + transportCost) * (platformPercent / 100.0f));
        float totalAmount = roundHalfUp(parcelValue + transportCost + hospitalFeeAmount + platformFeeAmount);

        return DeliveryCost.builder()
                .distanceKm(distanceKm)
                .appliedKmRate(rate)
                .parcelItemsTotal(parcelValue)
                .transportCost(transportCost)
                .hospitalPercent(hospitalPercent)
                .hospitalFeeAmount(hospitalFeeAmount)
                .platformPercent(platformPercent)
                .platformFeeAmount(platformFeeAmount)
                .totalAmount(totalAmount)
                .build();
    }

    public MonthlyRevenueReportDto generateMonthlyReport(UUID fleetId, int year, int month) {
        Fleet fleet = fleetRepository.findById(fleetId)
                .orElseThrow(() -> new IllegalArgumentException("Fleet not found with id: " + fleetId));

        List<Parcel> parcels = parcelRepository.findByFleetIdAndStatus(fleetId, ParcelStatus.DELIVERED);

        // Filter by month and year of delivery creation
        List<Parcel> monthlyDelivered = parcels.stream()
                .filter(p -> p.getCreatedAt().getYear() == year && p.getCreatedAt().getMonthValue() == month)
                .toList();

        Set<UUID> robotsUsed = new HashSet<>();
        float platformPercentageRevenue = 0.0f;

        for (Parcel parcel : monthlyDelivered) {
            if (parcel.getRobot() != null) {
                robotsUsed.add(parcel.getRobot().getId());
            }
            if (parcel.getDeliveryCost() != null) {
                platformPercentageRevenue += parcel.getDeliveryCost().getPlatformFeeAmount();
            }
        }
        platformPercentageRevenue = roundHalfUp(platformPercentageRevenue);

        float robotMonthlyRate = DEFAULT_ROBOT_MONTHLY_FEE;
        if (fleet.getPlatformManager() != null) {
            var policyOpt = platformPricePolicyRepository.findByPlatformManagerId(fleet.getPlatformManager().getId());
            if (policyOpt.isPresent()) {
                robotMonthlyRate = policyOpt.get().getRobotMonthlyFee();
            }
        }

        float robotFees = roundHalfUp(robotsUsed.size() * robotMonthlyRate);
        float totalAmount = roundHalfUp(robotFees + platformPercentageRevenue);

        return MonthlyRevenueReportDto.builder()
                .fleetId(fleet.getId())
                .fleetName(fleet.getFleetName())
                .year(year)
                .month(month)
                .robotsUsed(robotsUsed.size())
                .robotFees(robotFees)
                .platformPercentageRevenue(platformPercentageRevenue)
                .totalAmount(totalAmount)
                .build();
    }

    private float getApplicableKmRate(Fleet fleet, boolean requiresRefrigeration) {
        var policyOpt = instancePricePolicyRepository.findByFleetId(fleet.getId());
        if (policyOpt.isPresent()) {
            return requiresRefrigeration ? policyOpt.get().getRefrigeratedKmFee() : policyOpt.get().getBaseKmFee();
        }
        return requiresRefrigeration ? DEFAULT_REFRIGERATED_KM_FEE : DEFAULT_BASE_KM_FEE;
    }

    private float getHospitalPercentFee(Fleet fleet) {
        var policyOpt = instancePricePolicyRepository.findByFleetId(fleet.getId());
        return policyOpt.map(InstancePricePolicy::getParcelPercentFee).orElse(DEFAULT_HOSPITAL_PERCENT);
    }

    private float getPlatformPercentFee(Fleet fleet) {
        if (fleet.getPlatformManager() != null) {
            var policyOpt = platformPricePolicyRepository.findByPlatformManagerId(fleet.getPlatformManager().getId());
            if (policyOpt.isPresent()) {
                return policyOpt.get().getParcelPercentFee();
            }
        }
        return DEFAULT_PLATFORM_PERCENT;
    }
}
