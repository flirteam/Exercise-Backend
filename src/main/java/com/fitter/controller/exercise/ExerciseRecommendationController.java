//ExerciseRecommendationController
package com.fitter.controller.exercise;

import com.fitter.domain.exercise.ExerciseRecommendation;
import com.fitter.dto.response.ExerciseRecommendationResponse;
import com.fitter.dto.response.ExerciseProgressResponse;
import com.fitter.service.exercise.ExerciseRecommendationService;
import com.fitter.service.exercise.ExerciseProgressService;
import com.fitter.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/exercise-recommendations")
@RequiredArgsConstructor
public class ExerciseRecommendationController {

    private final ExerciseRecommendationService recommendationService;
    private final ExerciseProgressService progressService;
    private final JwtTokenProvider jwtTokenProvider; // JWT 유틸리티 클래스

    @PostMapping
    public ResponseEntity<ExerciseRecommendationResponse> createRecommendation(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        ExerciseRecommendationResponse response = recommendationService.createRecommendation(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ExerciseRecommendationResponse>> getUserRecommendations(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        List<ExerciseRecommendationResponse> recommendations =
                recommendationService.getUserRecommendations(userId, startDate, endDate);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/{recommendationId}/complete")
    public ResponseEntity<ExerciseRecommendationResponse> completeRecommendation(
            @RequestHeader("Authorization") String token,
            @PathVariable Long recommendationId,
            @RequestParam Integer actualDuration,
            @RequestParam(required = false) Integer difficultyRating,
            @RequestParam(required = false) String feedback) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        // 1. 먼저 추천 운동 조회
        ExerciseRecommendation recommendation = recommendationService.getRecommendation(recommendationId);

        // 2. 추천 운동의 routineId로 진행 상황 기록
        ExerciseProgressResponse progressResponse =
                progressService.recordProgress(userId, recommendation.getExerciseRoutine().getId(),
                        actualDuration, difficultyRating, feedback);

        // 3. 추천 운동 완료 처리
        ExerciseRecommendationResponse response =
                recommendationService.completeRecommendation(recommendationId, progressResponse.getFeedback());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress")
    public ResponseEntity<List<ExerciseProgressResponse>> getUserProgress(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        List<ExerciseProgressResponse> progress =
                progressService.getUserProgress(userId, startDate, endDate);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/analysis/achievement-rate")
    public ResponseEntity<Double> getAchievementRate(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        Double achievementRate = progressService.calculateGoalAchievementRate(userId, date);
        return ResponseEntity.ok(achievementRate);
    }

    @GetMapping("/analysis/efficiency")
    public ResponseEntity<Double> getExerciseEfficiency(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        Double efficiency = progressService.analyzeExerciseEfficiency(userId, startDate, endDate);
        return ResponseEntity.ok(efficiency);
    }

    @GetMapping("/analysis/difficulty")
    public ResponseEntity<String> getDifficultyAnalysis(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String analysis = progressService.analyzeDifficultyAppropriateness(userId);
        return ResponseEntity.ok(analysis);
    }
}
