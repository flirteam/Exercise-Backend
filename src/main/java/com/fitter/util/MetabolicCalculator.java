// MetabolicCalculator.java
package com.fitter.util;

import com.fitter.domain.user.UserPhysicalInfo;
import com.fitter.domain.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MetabolicCalculator {

    private static final double WEIGHT_FACTOR = 10.0;
    private static final double HEIGHT_FACTOR = 6.25;
    private static final double AGE_FACTOR = 5.0;
    private static final double MALE_CONSTANT = 5.0;
    private static final double FEMALE_CONSTANT = -161.0;

    // BMR (기초 대사량) 계산 - Mifflin-St Jeor 공식
    public double calculateBMR(UserPhysicalInfo physicalInfo) {
        double bmr = (WEIGHT_FACTOR * physicalInfo.getCurrentWeight()) +
                (HEIGHT_FACTOR * physicalInfo.getHeight()) -
                (AGE_FACTOR * physicalInfo.getAge());

        return physicalInfo.getGender() == UserPhysicalInfo.Gender.Male ?
                bmr + MALE_CONSTANT : bmr + FEMALE_CONSTANT;
    }

    // AMR (활동 대사량) 계산
    public double calculateAMR(UserPhysicalInfo physicalInfo) {
        double bmr = calculateBMR(physicalInfo);
        double activityFactor = getActivityFactor(physicalInfo.getActivityLevel());
        return bmr * activityFactor;
    }

    // 활동 계수 반환
    private double getActivityFactor(int activityLevel) {
        return switch (activityLevel) {
            case 1 -> 1.2; // 비활동적
            case 2 -> 1.375;  // 약간 활동적
            case 3 -> 1.55; // 중간 정돈 활동적
            case 4 -> 1.725;   // 매우 활동적
            default -> 1.2;
        };
    }

    // BMI 계산
    public double calculateBMI(UserPhysicalInfo physicalInfo) {
        double heightInMeters = physicalInfo.getHeight() / 100.0;
        return physicalInfo.getCurrentWeight() / (heightInMeters * heightInMeters);
    }

    // 체지방률 계산 (BMI 방법 - 추정치)
    public double calculateBodyFatPercentage(UserPhysicalInfo physicalInfo) {
        double bmi = calculateBMI(physicalInfo);
        int age = physicalInfo.getAge();
        boolean isMale = physicalInfo.getGender() == UserPhysicalInfo.Gender.Male;

        return (1.20 * bmi) + (0.23 * age) - (10.8 * (isMale ? 1 : 0)) - 5.4;
    }

    // 목표 체중까지 필요한 일일 권장 칼로리 섭취량
    public double calculateTargetCalories(UserPhysicalInfo physicalInfo) {
        double amr = calculateAMR(physicalInfo);
        double currentWeight = physicalInfo.getCurrentWeight();
        double targetWeight = physicalInfo.getTargetWeight();

        // 1kg = 7700kcal, 8주간 목표 달성 가정
        double totalCaloriesDifference = (targetWeight - currentWeight) * 7700;
        double dailyCaloriesDifference = totalCaloriesDifference / (8 * 7); // 8주 * 7일

        return amr + dailyCaloriesDifference;
    }

    // 운동 강도별 권장 심박수 범위 계산 (Karvonen 공식)
    public HeartRateZone calculateHeartRateZone(UserPhysicalInfo physicalInfo) {
        int age = physicalInfo.getAge();
        int maxHeartRate = 220 - age;
        int restingHeartRate = estimateRestingHeartRate(physicalInfo);

        return new HeartRateZone(
                calculateTargetHeartRate(maxHeartRate, restingHeartRate, 0.6),  // 낮은 강도
                calculateTargetHeartRate(maxHeartRate, restingHeartRate, 0.7),  // 중간 강도
                calculateTargetHeartRate(maxHeartRate, restingHeartRate, 0.8)   // 높은 강도
        );
    }

    private int estimateRestingHeartRate(UserPhysicalInfo physicalInfo) {
        // 활동 레벨에 따른 안정시 심박수 추정
        return switch (physicalInfo.getActivityLevel()) {
            case 1 -> 75;  // 비활동적
            case 2 -> 70;  // 약간 활동적
            case 3 -> 65;  // 중간 정도 활동적
            default -> 60; // 매우 활동적
        };
    }

    private int calculateTargetHeartRate(int maxHR, int restingHR, double intensity) {
        return (int) Math.round(((maxHR - restingHR) * intensity) + restingHR);
    }

    @lombok.Value
    public static class HeartRateZone {
        int lowIntensity;   // 60% of HRR
        int moderateIntensity; // 70% of HRR
        int highIntensity;     // 80% of HRR
    }
}