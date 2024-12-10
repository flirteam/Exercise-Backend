// ExerciseRecommendationResponse.java
package com.fitter.dto.response;

import com.fitter.domain.exercise.ExerciseRecommendation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseRecommendationResponse {
    private Long id;
    private Long userId;
    private ExerciseRoutineResponse exerciseRoutine;
    private LocalDate recommendationDate;
    private Double caloriesBurned;
    private Boolean completionStatus;
    private String feedback;
    private LocalDateTime createdAt;

    // 추천 이유 정보
    private RecommendationReason recommendationReason;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationReason {
        private Double userBmi;
        private Integer userActivityLevel;
        private Double targetWeightDiff;
        private String reasonDescription;
    }

    public static ExerciseRecommendationResponse from(ExerciseRecommendation recommendation) {
        return ExerciseRecommendationResponse.builder()
                .id(recommendation.getId())
                .userId(recommendation.getUser().getId())
                .exerciseRoutine(ExerciseRoutineResponse.from(recommendation.getExerciseRoutine()))
                .recommendationDate(recommendation.getRecommendationDate())
                .caloriesBurned(recommendation.getCaloriesBurned())
                .completionStatus(recommendation.getCompletionStatus())
                .feedback(recommendation.getFeedback())
                .createdAt(recommendation.getCreatedAt())
                .recommendationReason(buildRecommendationReason(recommendation))
                .build();
    }

    private static RecommendationReason buildRecommendationReason(ExerciseRecommendation recommendation) {
        double userBmi = recommendation.getUser().getPhysicalInfo().getBmi();
        int activityLevel = recommendation.getUser().getPhysicalInfo().getActivityLevel();
        double targetWeightDiff = Math.abs(
                recommendation.getUser().getPhysicalInfo().getCurrentWeight() -
                        recommendation.getUser().getPhysicalInfo().getTargetWeight()
        );

        String reasonDescription = generateReasonDescription(userBmi, activityLevel, targetWeightDiff);

        return RecommendationReason.builder()
                .userBmi(userBmi)
                .userActivityLevel(activityLevel)
                .targetWeightDiff(targetWeightDiff)
                .reasonDescription(reasonDescription)
                .build();
    }

    private static String generateReasonDescription(double bmi, int activityLevel, double weightDiff) {
        StringBuilder reason = new StringBuilder("이 운동을 추천하는 이유: ");

        if (bmi < 18.5) {
            reason.append("체중 증가가 필요한 상태이며, ");
        } else if (bmi > 25) {
            reason.append("체중 감량이 필요한 상태이며, ");
        }

        if (activityLevel <= 2) {
            reason.append("현재 활동량이 낮은 편이므로 점진적인 운동 강도 증가를 추천드립니다. ");
        } else {
            reason.append("현재 활동량이 적절한 수준이므로 현재 강도를 유지하는 것을 추천드립니다. ");
        }

        if (weightDiff > 5) {
            reason.append("목표 체중까지 ").append(String.format("%.1f", weightDiff)).append("kg 차이가 있어 ");
            reason.append("꾸준한 운동이 필요합니다.");
        }

        return reason.toString();
    }
}
