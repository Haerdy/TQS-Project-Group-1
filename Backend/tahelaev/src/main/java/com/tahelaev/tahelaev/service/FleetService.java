package com.tahelaev.tahelaev.service;

import com.tahelaev.tahelaev.domain.Fleet;
import com.tahelaev.tahelaev.domain.InstancePricePolicy;
import com.tahelaev.tahelaev.domain.PlatformManager;
import com.tahelaev.tahelaev.domain.SystemAdmin;
import com.tahelaev.tahelaev.dto.FleetCreateRequestDto;
import com.tahelaev.tahelaev.dto.FleetDto;
import com.tahelaev.tahelaev.dto.InstancePricePolicyDto;
import com.tahelaev.tahelaev.repository.FleetRepository;
import com.tahelaev.tahelaev.repository.InstancePricePolicyRepository;
import com.tahelaev.tahelaev.repository.PlatformManagerRepository;
import com.tahelaev.tahelaev.repository.SystemAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FleetService {

    private final FleetRepository fleetRepository;
    private final SystemAdminRepository systemAdminRepository;
    private final PlatformManagerRepository platformManagerRepository;
    private final InstancePricePolicyRepository instancePricePolicyRepository;

    @Transactional
    public FleetDto createFleet(FleetCreateRequestDto request) {
        // US01: Reject duplicate hospital name
        if (fleetRepository.existsByFleetNameIgnoreCase(request.getFleetName())) {
            throw new IllegalArgumentException("A hospital with this name already exists");
        }

        PlatformManager platformManager = null;
        if (request.getPlatformManagerId() != null) {
            platformManager = platformManagerRepository.findById(request.getPlatformManagerId()).orElse(null);
        }

        SystemAdmin admin = SystemAdmin.builder()
                .name(request.getSystemAdminName())
                .email(request.getSystemAdminEmail())
                .password(request.getSystemAdminPassword())
                .build();
        admin = systemAdminRepository.save(admin);

        Fleet fleet = Fleet.builder()
                .fleetName(request.getFleetName())
                .systemAdmin(admin)
                .platformManager(platformManager)
                .noOfRobots(0)
                .noOfActiveRobots(0)
                .build();
        fleet = fleetRepository.save(fleet);

        admin.setFleet(fleet);
        systemAdminRepository.save(admin);

        // Initialize default instance price policy
        InstancePricePolicy policy = InstancePricePolicy.builder()
                .baseKmFee(1.50f)
                .refrigeratedKmFee(2.00f)
                .parcelPercentFee(10.0f)
                .fleet(fleet)
                .build();
        instancePricePolicyRepository.save(policy);
        fleet.setInstancePricePolicy(policy);

        return toDto(fleet);
    }

    public FleetDto getFleetById(UUID fleetId) {
        Fleet fleet = fleetRepository.findById(fleetId)
                .orElseThrow(() -> new IllegalArgumentException("Fleet not found with id: " + fleetId));
        return toDto(fleet);
    }

    public List<FleetDto> getAllFleets() {
        return fleetRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public InstancePricePolicyDto updateInstancePricePolicy(UUID fleetId, InstancePricePolicyDto dto) {
        Fleet fleet = fleetRepository.findById(fleetId)
                .orElseThrow(() -> new IllegalArgumentException("Fleet not found with id: " + fleetId));

        InstancePricePolicy policy = instancePricePolicyRepository.findByFleetId(fleetId)
                .orElse(InstancePricePolicy.builder().fleet(fleet).build());

        policy.setBaseKmFee(dto.getBaseKmFee());
        policy.setRefrigeratedKmFee(dto.getRefrigeratedKmFee());
        policy.setParcelPercentFee(dto.getParcelPercentFee());

        policy = instancePricePolicyRepository.save(policy);

        return InstancePricePolicyDto.builder()
                .id(policy.getId())
                .baseKmFee(policy.getBaseKmFee())
                .refrigeratedKmFee(policy.getRefrigeratedKmFee())
                .parcelPercentFee(policy.getParcelPercentFee())
                .fleetId(fleet.getId())
                .build();
    }

    public FleetDto toDto(Fleet fleet) {
        InstancePricePolicyDto policyDto = null;
        if (fleet.getInstancePricePolicy() != null) {
            InstancePricePolicy p = fleet.getInstancePricePolicy();
            policyDto = InstancePricePolicyDto.builder()
                    .id(p.getId())
                    .baseKmFee(p.getBaseKmFee())
                    .refrigeratedKmFee(p.getRefrigeratedKmFee())
                    .parcelPercentFee(p.getParcelPercentFee())
                    .fleetId(fleet.getId())
                    .build();
        }

        return FleetDto.builder()
                .id(fleet.getId())
                .fleetName(fleet.getFleetName())
                .noOfRobots(fleet.getNoOfRobots())
                .noOfActiveRobots(fleet.getNoOfActiveRobots())
                .systemAdminId(fleet.getSystemAdmin() != null ? fleet.getSystemAdmin().getId() : null)
                .systemAdminEmail(fleet.getSystemAdmin() != null ? fleet.getSystemAdmin().getEmail() : null)
                .platformManagerId(fleet.getPlatformManager() != null ? fleet.getPlatformManager().getId() : null)
                .instancePricePolicy(policyDto)
                .build();
    }
}
