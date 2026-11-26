package com.tahelaev.tahelaev.service;

import com.tahelaev.tahelaev.domain.EndUser;
import com.tahelaev.tahelaev.domain.Fleet;
import com.tahelaev.tahelaev.domain.PlatformManager;
import com.tahelaev.tahelaev.domain.SystemAdmin;
import com.tahelaev.tahelaev.domain.User;
import com.tahelaev.tahelaev.dto.UserDto;
import com.tahelaev.tahelaev.repository.EndUserRepository;
import com.tahelaev.tahelaev.repository.FleetRepository;
import com.tahelaev.tahelaev.repository.PlatformManagerRepository;
import com.tahelaev.tahelaev.repository.SystemAdminRepository;
import com.tahelaev.tahelaev.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EndUserRepository endUserRepository;
    private final SystemAdminRepository systemAdminRepository;
    private final PlatformManagerRepository platformManagerRepository;
    private final FleetRepository fleetRepository;

    @Transactional
    public UserDto createEndUser(String name, String email, String password, String address, UUID fleetId) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        Fleet fleet = null;
        if (fleetId != null) {
            fleet = fleetRepository.findById(fleetId)
                    .orElseThrow(() -> new IllegalArgumentException("Fleet not found with id: " + fleetId));
        }

        EndUser user = EndUser.builder()
                .name(name)
                .email(email)
                .password(password)
                .address(address)
                .fleet(fleet)
                .build();

        return toDto(endUserRepository.save(user));
    }

    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        return toDto(user);
    }

    public List<UserDto> getUsersByFleet(UUID fleetId) {
        return endUserRepository.findByFleetId(fleetId).stream().map(this::toDto).toList();
    }

    public UserDto toDto(User user) {
        String role = "UNKNOWN";
        UUID fleetId = null;
        String address = null;

        if (user instanceof PlatformManager) {
            role = "PLATFORM_MANAGER";
        } else if (user instanceof SystemAdmin sa) {
            role = "SYSTEM_ADMIN";
            fleetId = sa.getFleet() != null ? sa.getFleet().getId() : null;
        } else if (user instanceof EndUser eu) {
            role = "END_USER";
            fleetId = eu.getFleet() != null ? eu.getFleet().getId() : null;
            address = eu.getAddress();
        }

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(role)
                .fleetId(fleetId)
                .address(address)
                .build();
    }
}
