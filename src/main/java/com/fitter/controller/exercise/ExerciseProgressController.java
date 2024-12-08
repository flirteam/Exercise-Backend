//ExerciseProgressController.java
package com.fitter.controller.exercise;

import com.fitter.dto.response.ExerciseProgressResponse;
import com.fitter.service.exercise.ExerciseProgressService;
import com.fitter.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exercise-progress")
@RequiredArgsConstructor
public class ExerciseProgressController {

    private final ExerciseProgressService progressService;
    private final JwtTokenProvider jwtTokenProvider; // JWT 유틸리티 클래스

    @PostMapping("/record/{routineId}")
    public ResponseEntity<ExerciseProgressResponse> recordProgress(
            @RequestHeader("Authorization") String token,
            @PathVariable Long routineId,
            @RequestParam int actualDuration,
            @RequestParam(required = false) Integer difficultyRating,
            @RequestParam(required = false) String feedback) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        ExerciseProgressResponse response = progressService.recordProgress(
                userId, routineId, actualDuration, difficultyRating, feedback);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ExerciseProgressResponse>> getProgressHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        List<ExerciseProgressResponse> history = progressService.getUserProgress(userId, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/daily-achievement")
    public ResponseEntity<Double> getDailyAchievementRate(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        double achievementRate = progressService.calculateGoalAchievementRate(userId, date);
        return ResponseEntity.ok(achievementRate);
    }

    @GetMapping("/efficiency")
    public ResponseEntity<Map<String, Object>> getExerciseEfficiency(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        double efficiency = progressService.analyzeExerciseEfficiency(userId, startDate, endDate);
        String analysis = progressService.analyzeDifficultyAppropriateness(userId);

        return ResponseEntity.ok(Map.of(
                "efficiency", efficiency,
                "analysis", analysis
        ));
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<Map<String, Object>> getMonthlySummary(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") String yearMonth) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        LocalDate startDate = LocalDate.parse(yearMonth + "-01");
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<ExerciseProgressResponse> monthlyProgress =
                progressService.getUserProgress(userId, startDate, endDate);

        // 월간 통계 계산
        double averageAchievementRate = monthlyProgress.stream()
                .mapToDouble(progress -> progress.getProgressAnalysis().getAchievementRate())
                .average()
                .orElse(0.0);

        double averageEfficiencyScore = monthlyProgress.stream()
                .mapToDouble(progress -> progress.getProgressAnalysis().getEfficiencyScore())
                .average()
                .orElse(0.0);

        long totalExerciseDays = monthlyProgress.size();

        return ResponseEntity.ok(Map.of(
                "yearMonth", yearMonth,
                "averageAchievementRate", averageAchievementRate,
                "averageEfficiencyScore", averageEfficiencyScore,
                "totalExerciseDays", totalExerciseDays,
                "progressList", monthlyProgress
        ));
    }

    @GetMapping("/activity-trends")
    public ResponseEntity<Map<String, Object>> getActivityTrends(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        List<ExerciseProgressResponse> progressList =
                progressService.getUserProgress(userId, startDate, endDate);

        // 기간별 운동 추세 분석
        double averageDailyDuration = progressList.stream()
                .mapToInt(progress -> progress.getActualDurationMinutes())
                .average()
                .orElse(0.0);

        double averageCaloriesBurned = progressList.stream()
                .mapToDouble(progress -> progress.getActualCaloriesBurned())
                .average()
                .orElse(0.0);

        double consistencyRate = (double) progressList.size() /
                (startDate.until(endDate).getDays() + 1) * 100;

        return ResponseEntity.ok(Map.of(
                "period", Map.of("start", startDate, "end", endDate),
                "averageDailyDuration", averageDailyDuration,
                "averageCaloriesBurned", averageCaloriesBurned,
                "consistencyRate", consistencyRate,
                "totalWorkouts", progressList.size()
        ));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<String> getProgressBasedRecommendations(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String recommendations = progressService.analyzeDifficultyAppropriateness(userId);
        return ResponseEntity.ok(recommendations);
    }
}
