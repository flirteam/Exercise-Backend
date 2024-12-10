// CalorieCalculator.java
package com.fitter.util;

import com.fitter.domain.exercise.ExerciseBase;
import com.fitter.domain.exercise.ExerciseRoutine;
import com.fitter.domain.user.UserPhysicalInfo;
import org.springframework.stereotype.Component;

@Component
public class CalorieCalculator {

    private static final double BASE_WEIGHT = 70.0; // 기준 체중 (kg)
    private static final double WEIGHT_ADJUSTMENT_FACTOR = 0.75; // 체중 보정 계수

    // 운동별 실제 칼로리 소모량 계산
    public double calculateCaloriesBurned(
            UserPhysicalInfo physicalInfo,
            ExerciseBase exercise,
            int durationMinutes,
            ExerciseRoutine.IntensityLevel intensity) {

        // 기본 칼로리 소모량 계산
        double baseCalories = exercise.getBaseCaloriesBurned() * durationMinutes;

        // 체중 보정
        double weightFactor = calculateWeightFactor(physicalInfo.getCurrentWeight());

        // 강도 보정
        double intensityFactor = getIntensityFactor(intensity);

        // 활동량 보정
        double activityFactor = getActivityLevelFactor(physicalInfo.getActivityLevel());

        return baseCalories * weightFactor * intensityFactor * activityFactor;
    }

    // 체중 보정 계수 계산
    private double calculateWeightFactor(double weight) {
        double weightDifference = weight - BASE_WEIGHT;
        return 1.0 + (weightDifference / BASE_WEIGHT) * WEIGHT_ADJUSTMENT_FACTOR;
    }

    // 강도별 보정 계수
    private double getIntensityFactor(ExerciseRoutine.IntensityLevel intensity) {
        return switch (intensity) {
            case 낮음 -> 0.8;
            case 중간 -> 1.0;
            case 높음 -> 1.2;
        };
    }

    // 활동량별 보정 계수
    private double getActivityLevelFactor(int activityLevel) {
        return switch (activityLevel) {
            case 1 -> 0.8;  // 비활동적
            case 2 -> 0.9;  // 약간
            case 3 -> 1.0;  // 중간
            case 4 -> 1.1;  // 매우 활동적
            default -> 1.0;
        };
    }

    // 일일 목표 칼로리 계산
    public double calculateDailyTargetCalories(UserPhysicalInfo physicalInfo, double weightChangeGoal) {
        double bmr = new MetabolicCalculator().calculateBMR(physicalInfo);
        double activityFactor = getActivityLevelFactor(physicalInfo.getActivityLevel());

        // 기본 유지 칼로리
        double maintenanceCalories = bmr * activityFactor;

        // 체중 변화 목표에 따른 조정 (1kg = 7700kcal)
        double adjustmentCalories = (weightChangeGoal * 7700) / 7; // 주간 목표를 일일로 변환

        return maintenanceCalories + adjustmentCalories;
    }

    // 운동 강도별 예상 칼로리 소모량 범위
    public CalorieRange calculateCalorieRange(UserPhysicalInfo physicalInfo, ExerciseBase exercise, int durationMinutes) {
        double lowIntensity = calculateCaloriesBurned(physicalInfo, exercise, durationMinutes, ExerciseRoutine.IntensityLevel.낮음);
        double moderateIntensity = calculateCaloriesBurned(physicalInfo, exercise, durationMinutes, ExerciseRoutine.IntensityLevel.중간);
        double highIntensity = calculateCaloriesBurned(physicalInfo, exercise, durationMinutes, ExerciseRoutine.IntensityLevel.높음);

        return new CalorieRange(lowIntensity, moderateIntensity, highIntensity);
    }

    @lombok.Value
    public static class CalorieRange {
        double minCalories;
        double avgCalories;
        double maxCalories;
    }
}
