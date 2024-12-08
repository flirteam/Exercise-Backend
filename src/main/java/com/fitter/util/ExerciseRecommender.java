// ExerciseRecommender.java
package com.fitter.util;

import com.fitter.domain.exercise.*;
import com.fitter.domain.user.UserPhysicalInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExerciseRecommender {

    private final MetabolicCalculator metabolicCalculator;

    // 사용자 특성에 맞는 운동 추천
    public List<ExerciseBase> recommendExercises(
            UserPhysicalInfo physicalInfo,
            ExercisePreference preference,
            List<ExerciseBase> availableExercises) {

        double bmi = metabolicCalculator.calculateBMI(physicalInfo);
        double bodyFatPercentage = metabolicCalculator.calculateBodyFatPercentage(physicalInfo);

        return availableExercises.stream()
                .filter(exercise -> isAppropriateForBMI(exercise, bmi))
                .filter(exercise -> matchesPreference(exercise, preference))
                .sorted((e1, e2) -> compareExerciseSuitability(e1, e2, physicalInfo, preference))
                .limit(5)
                .collect(Collectors.toList());
    }

    // BMI에 따른 운동 적합성 검사
    private boolean isAppropriateForBMI(ExerciseBase exercise, double bmi) {
        if (bmi < 18.5) {
            // 저체중: 근력 운동 우선
            return exercise.getExerciseCategory() == ExerciseBase.ExerciseCategory.상체근력운동 ||
                    exercise.getExerciseCategory() == ExerciseBase.ExerciseCategory.하체근력운동;
        } else if (bmi > 25) {
            // 과체중: 유산소 운동 우선
            return exercise.getExerciseCategory() == ExerciseBase.ExerciseCategory.유산소운동 ||
                    exercise.getExerciseCategory() == ExerciseBase.ExerciseCategory.전신강화운동;
        }
        return true;
    }

    // 사용자 선호도와 운동 매칭
    private boolean matchesPreference(ExerciseBase exercise, ExercisePreference preference) {
        if (preference == null) return true;

        return (preference.getPreferredBodyPart() == null ||
                preference.getPreferredBodyPart() == exercise.getBodyPart()) &&
                (preference.getPreferredCategory() == null ||
                        preference.getPreferredCategory() == exercise.getExerciseCategory());
    }

    // 운동 적합도 비교
    private int compareExerciseSuitability(
            ExerciseBase e1,
            ExerciseBase e2,
            UserPhysicalInfo physicalInfo,
            ExercisePreference preference) {

        double score1 = calculateSuitabilityScore(e1, physicalInfo, preference);
        double score2 = calculateSuitabilityScore(e2, physicalInfo, preference);

        return Double.compare(score2, score1);
    }

    // 운동 적합도 점수 계산
    private double calculateSuitabilityScore(
            ExerciseBase exercise,
            UserPhysicalInfo physicalInfo,
            ExercisePreference preference) {

        double score = 0.0;

        // 기본 점수: BMI 기반
        double bmi = metabolicCalculator.calculateBMI(physicalInfo);
        score += getBMIBasedScore(exercise, bmi);

        // 선호도 점수
        if (preference != null) {
            score += getPreferenceScore(exercise, preference);
        }

        // 활동량 기반 점수
        score += getActivityLevelScore(exercise, physicalInfo.getActivityLevel());

        return score;
    }

    // BMI 기반 점수
    private double getBMIBasedScore(ExerciseBase exercise, double bmi) {
        if (bmi < 18.5) {
            return exercise.getExerciseCategory() == ExerciseBase.ExerciseCategory.상체근력운동 ||
                    exercise.getExerciseCategory() == ExerciseBase.ExerciseCategory.하체근력운동 ? 2.0 : 1.0;
        } else if (bmi > 25) {
            return exercise.getExerciseCategory() == ExerciseBase.ExerciseCategory.유산소운동 ? 2.0 : 1.0;
        }
        return 1.5;
    }

    // 선호도 기반 점수
    private double getPreferenceScore(ExerciseBase exercise, ExercisePreference preference) {
        double score = 0.0;

        if (preference.getPreferredBodyPart() == exercise.getBodyPart()) {
            score += 1.0;
        }

        if (preference.getPreferredCategory() == exercise.getExerciseCategory()) {
            score += 1.0;
        }

        return score;
    }

    // 활동량 기반 점수
    private double getActivityLevelScore(ExerciseBase exercise, int activityLevel) {
        return switch (activityLevel) {
            case 1 -> exercise.getExerciseCategory() == ExerciseBase.ExerciseCategory.전신강화운동 ? 1.5 : 1.0;
            case 2 -> 1.0;
            case 3 -> exercise.getExerciseCategory() == ExerciseBase.ExerciseCategory.유연성및스트레칭운동 ? 1.5 : 1.0;
            default -> exercise.getExerciseCategory() == ExerciseBase.ExerciseCategory.유산소운동 ? 1.5 : 1.0;
        };
    }

    // 권장 운동 시간 계산
    public int calculateRecommendedDuration(UserPhysicalInfo physicalInfo, ExerciseBase exercise) {
        double bmi = metabolicCalculator.calculateBMI(physicalInfo);
        int baseDuration = 30; // 기본 30분

        // BMI에 따른 조정
        if (bmi < 18.5 || bmi > 25) {
            baseDuration = 20; // 체중이 정상 범위를 벗어난 경우 시작은 20분
        }

        // 활동 레벨에 따른 조정
        int activityLevelAdjustment = switch (physicalInfo.getActivityLevel()) {
            case 1 -> 10;  // 매우 활동적
            case 2 -> 5;   // 중간
            case 3 -> 0;   // 약간
            default -> -5; // 비활동적
        };

        return Math.min(baseDuration + activityLevelAdjustment, 60); // 최대 60분
    }
}