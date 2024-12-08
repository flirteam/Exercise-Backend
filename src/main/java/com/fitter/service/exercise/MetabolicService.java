// MetabolicService.java
package com.fitter.service.exercise;

import com.fitter.domain.user.UserPhysicalInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetabolicService {

    // 기초 대사량 (BMR) 계산 - Mifflin-St Jeor 공식
    public double calculateBMR(UserPhysicalInfo physicalInfo) {
        double weight = physicalInfo.getCurrentWeight();
        double height = physicalInfo.getHeight();
        int age = physicalInfo.getAge();
        boolean isMale = physicalInfo.getGender() == UserPhysicalInfo.Gender.Male;

        // Mifflin-St Jeor 공식
        double bmr = (10 * weight) + (6.25 * height) - (5 * age);
        return isMale ? bmr + 5 : bmr - 161;
    }

    // 활동 대사량 (AMR) 계산
    public double calculateAMR(UserPhysicalInfo physicalInfo) {
        double bmr = calculateBMR(physicalInfo);
        double activityFactor = getActivityFactor(physicalInfo.getActivityLevel());
        return bmr * activityFactor;
    }

    // 활동 계수 반환
    private double getActivityFactor(int activityLevel) {
        return switch (activityLevel) {
            case 1 -> 1.725; // 매우 활동적
            case 2 -> 1.55;  // 중간
            case 3 -> 1.375; // 약간
            case 4 -> 1.2;   // 비활동적
            default -> 1.2;
        };
    }

    // BMI 계산
    public double calculateBMI(UserPhysicalInfo physicalInfo) {
        double heightInMeters = physicalInfo.getHeight() / 100;
        return physicalInfo.getCurrentWeight() / (heightInMeters * heightInMeters);
    }

    // 체지방률 계산 (BMI 방법 - 추정치)
    public double calculateBodyFatPercentage(UserPhysicalInfo physicalInfo) {
        double bmi = calculateBMI(physicalInfo);
        boolean isMale = physicalInfo.getGender() == UserPhysicalInfo.Gender.Male;
        int age = physicalInfo.getAge();

        return (1.20 * bmi) + (0.23 * age) - (10.8 * (isMale ? 1 : 0)) - 5.4;
    }

    // 목표 체중까지 필요한 일일 권장 칼로리 섭취량 계산
    public double calculateRecommendedDailyCalories(UserPhysicalInfo physicalInfo) {
        double amr = calculateAMR(physicalInfo);
        double currentWeight = physicalInfo.getCurrentWeight();
        double targetWeight = physicalInfo.getTargetWeight();

        // 1kg = 7700kcal, 8주간 목표 달성 가정
        double totalCaloriesDifference = (targetWeight - currentWeight) * 7700;
        double dailyCaloriesDifference = totalCaloriesDifference / (8 * 7); // 8주 * 7일

        return amr + dailyCaloriesDifference;
    }

    // 신체 활동 지수 계산 (PAL: Physical Activity Level)
    public double calculatePAL(UserPhysicalInfo physicalInfo) {
        return calculateAMR(physicalInfo) / calculateBMR(physicalInfo);
    }
}