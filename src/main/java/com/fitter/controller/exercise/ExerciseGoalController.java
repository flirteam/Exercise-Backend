//ExerciseGoalController.java
package com.fitter.controller.exercise;

import com.fitter.domain.exercise.ExerciseGoal;
import com.fitter.dto.request.ExerciseGoalRequest;
import com.fitter.dto.response.ExerciseGoalResponse;
import com.fitter.service.exercise.ExerciseGoalService;
import com.fitter.service.exercise.ExerciseGoalService.GoalAchievementAnalysis;
import com.fitter.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseGoalController {

    private final ExerciseGoalService exerciseGoalService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/goals")
    public ResponseEntity<ExerciseGoalResponse> setExerciseGoal(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ExerciseGoalRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        return ResponseEntity.ok(exerciseGoalService.setExerciseGoal(userId, request));
    }

    @GetMapping("/goals/active")
    public ResponseEntity<ExerciseGoalResponse> getActiveGoal(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        return ResponseEntity.ok(exerciseGoalService.getActiveGoal(userId));
    }
}