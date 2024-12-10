// ExerciseGoalRequest.java
package com.fitter.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseGoalRequest {

    @NotNull(message = "일일 목표 칼로리는 필수입니다.")
    @Min(value = 100, message = "일일 목표 칼로리는 최소 100kcal 이상이어야 합니다.")
    @Max(value = 2000, message = "일일 목표 칼로리는 최대 2000kcal 이하여야 합니다.")
    private Double targetCaloriesPerDay;

    @NotNull(message = "일일 목표 운동 시간은 필수입니다.")
    @Min(value = 10, message = "일일 목표 운동 시간은 최소 10분 이상이어야 합니다.")
    @Max(value = 180, message = "일일 목표 운동 시간은 최대 180분 이하여야 합니다.")
    private Integer targetExerciseMinutesPerDay;

    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDate startDate;

    // 종료 날짜 (선택)
    private LocalDate endDate;

    // 유효성 검사를 위한 커스텀 메서드들
    public boolean isValidDateRange() {
        if (endDate == null) {
            return true;
        }
        return !endDate.isBefore(startDate);
    }

    public boolean isValidStartDate() {
        return !startDate.isBefore(LocalDate.now());
    }

    // BMI와 활동량에 따른 권장 목표 칼로리 계산
    public boolean isWithinRecommendedCalorieRange(double bmi, int activityLevel) {
        double baseCalories = 500.0; // 기본 권장 칼로리

        // BMI에 따른 조정
        if (bmi < 18.5) {
            baseCalories *= 0.8;  // 저체중
        } else if (bmi > 25) {
            baseCalories *= 1.2;  // 과체중
        }

        // 활동량에 따른 조정
        double activityFactor = switch (activityLevel) {
            case 1 -> 0.7;  // 비활동적
            case 2 -> 0.9;  // 약간
            case 3 -> 1.1;  // 중간
            case 4 -> 1.3;  // 매우 활동적
            default -> 1.0;
        };

        double recommendedCalories = baseCalories * activityFactor;
        double tolerance = 200.0; // 허용 오차

        return Math.abs(targetCaloriesPerDay - recommendedCalories) <= tolerance;
    }
}