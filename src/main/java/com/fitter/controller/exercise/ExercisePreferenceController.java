// ExercisePreferenceController.java
package com.fitter.controller.exercise;

import com.fitter.dto.request.ExercisePreferenceRequest;
import com.fitter.dto.response.ExercisePreferenceResponse;
import com.fitter.service.exercise.ExercisePreferenceService;
import com.fitter.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/exercises/preferences")
@RequiredArgsConstructor
public class ExercisePreferenceController {

    private final ExercisePreferenceService preferenceService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<ExercisePreferenceResponse> setPreference(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ExercisePreferenceRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        return ResponseEntity.ok(preferenceService.setPreference(userId, request));
    }

    @GetMapping
    public ResponseEntity<ExercisePreferenceResponse> getPreference(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        return ResponseEntity.ok(preferenceService.getPreference(userId));
    }

    @PutMapping
    public ResponseEntity<ExercisePreferenceResponse> updatePreference(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ExercisePreferenceRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        return ResponseEntity.ok(preferenceService.updatePreference(userId, request));
    }
}