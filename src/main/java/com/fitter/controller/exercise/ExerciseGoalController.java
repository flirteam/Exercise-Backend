//ExerciseGoalController.java
package com.fitter.controller.exercise;

import com.fitter.domain.exercise.ExerciseGoal;
import com.fitter.dto.request.ExerciseGoalRequest;
import com.fitter.dto.response.ExerciseGoalResponse;
import com.fitter.service.exercise.ExerciseGoalService;
import com.fitter.service.exercise.ExerciseGoalService.GoalAchievementAnalysis;
import com.fitter.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<ExerciseGoalResponse>> getActiveGoals(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        return ResponseEntity.ok(exerciseGoalService.getActiveGoals(userId));
    }

    @GetMapping("/goals")
    public ResponseEntity<List<ExerciseGoalResponse>> getAllGoals(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        return ResponseEntity.ok(exerciseGoalService.getAllGoals(userId, startDate, endDate));
    }

    @DeleteMapping("/goals/{goalId}")
    public ResponseEntity<Void> deleteGoal(
            @RequestHeader("Authorization") String token,
            @PathVariable Long goalId) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        exerciseGoalService.deleteGoal(userId, goalId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/goals/statistics")
    public ResponseEntity<List<Map<String, Object>>> getGoalStatistics(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        return ResponseEntity.ok(exerciseGoalService.getGoalStatistics(userId));
    }

    @GetMapping("/goals/trends")
    public ResponseEntity<List<Object[]>> getGoalTrends(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        return ResponseEntity.ok(exerciseGoalService.getGoalTrends(userId, startDate, endDate));
    }
}