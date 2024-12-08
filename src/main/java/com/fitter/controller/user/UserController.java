//UserController.java
package com.fitter.controller.user;

import com.fitter.dto.request.PhysicalInfoRequest;
import com.fitter.dto.request.UserRegistrationRequest;
import com.fitter.dto.request.LoginRequest;
import com.fitter.dto.response.UserPhysicalInfoResponse;
import com.fitter.dto.response.UserResponse;
import com.fitter.service.user.UserService;
import com.fitter.util.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/info")
    public ResponseEntity<UserResponse> addUserInfo(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody PhysicalInfoRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        UserResponse response = userService.addUserInfo(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<UserPhysicalInfoResponse> getUserInfo(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        UserPhysicalInfoResponse response = userService.getUserPhysicalInfo(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/info")
    public ResponseEntity<UserPhysicalInfoResponse> updateUserInfo(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody PhysicalInfoRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        UserPhysicalInfoResponse response = userService.updateUserPhysicalInfo(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-activity-level/{activityLevel}")
    public ResponseEntity<List<UserResponse>> getUsersByActivityLevel(@PathVariable Integer activityLevel) {
        List<UserResponse> users = userService.getUsersByActivityLevel(activityLevel);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/bmi-range")
    public ResponseEntity<List<UserResponse>> getUsersByBmiRange(
            @RequestParam Double minBmi,
            @RequestParam Double maxBmi) {
        List<UserResponse> users = userService.getUsersByBmiRange(minBmi, maxBmi);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/target-achieved")
    public ResponseEntity<List<UserResponse>> getUsersReachedTargetWeight(
            @RequestParam(defaultValue = "0.5") Double threshold) {
        List<UserResponse> users = userService.getUsersReachedTargetWeight(threshold);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/exercise-stats/{userId}")
    public ResponseEntity<Long> getUserExerciseCount(@PathVariable Long userId) {
        Long count = userService.getExerciseRoutineCount(userId);
        return ResponseEntity.ok(count);
    }
}
