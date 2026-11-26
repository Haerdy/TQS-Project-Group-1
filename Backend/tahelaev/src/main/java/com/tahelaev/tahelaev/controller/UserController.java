package com.tahelaev.tahelaev.controller;

import com.tahelaev.tahelaev.dto.UserDto;
import com.tahelaev.tahelaev.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/end-users")
    public ResponseEntity<UserDto> createEndUser(@RequestBody UserDto request) {
        UserDto user = userService.createEndUser(
                request.getName(),
                request.getEmail(),
                "password123", // default or provided password
                request.getAddress(),
                request.getFleetId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/fleet/{fleetId}")
    public ResponseEntity<List<UserDto>> getUsersByFleet(@PathVariable UUID fleetId) {
        return ResponseEntity.ok(userService.getUsersByFleet(fleetId));
    }
}
