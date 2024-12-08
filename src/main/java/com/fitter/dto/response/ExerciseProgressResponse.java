// ExerciseProgressResponse.java
package com.fitter.dto.response;

import com.fitter.domain.exercise.UserExerciseProgress;
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
public class ExerciseProgressResponse {
    private Long id;
    private Long userId;
    private ExerciseRoutineResponse exerciseRoutine;
    private LocalDate completedDate;
    private Integer actualDurationMinutes;
    private Double actualCaloriesBurned;
    private Integer difficultyRating;
    private String feedback;
    private LocalDateTime createdAt;

    // 운동 성과 분석 정보
    private ProgressAnalysis progressAnalysis;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressAnalysis {
        private double achievementRate;
        private int efficiencyScore;
        private String analysisDescription;
        private String recommendation;
    }

    public static ExerciseProgressResponse from(UserExerciseProgress progress) {
        return ExerciseProgressResponse.builder()
                .id(progress.getId())
                .userId(progress.getUser().getId())
                .exerciseRoutine(ExerciseRoutineResponse.from(progress.getExerciseRoutine()))
                .completedDate(progress.getCompletedDate())
                .actualDurationMinutes(progress.getActualDurationMinutes())
                .actualCaloriesBurned(progress.getActualCaloriesBurned())
                .difficultyRating(progress.getDifficultyRating())
                .feedback(progress.getFeedback())
                .createdAt(progress.getCreatedAt())
                .progressAnalysis(buildProgressAnalysis(progress))
                .build();
    }

    private static ProgressAnalysis buildProgressAnalysis(UserExerciseProgress progress) {
        double achievementRate = progress.calculateAchievementRate();
        int efficiencyScore = progress.calculateEfficiencyScore();

        String analysisDescription = generateAnalysisDescription(
                achievementRate, efficiencyScore, progress.getDifficultyRating()
        );

        String recommendation = generateRecommendation(
                achievementRate, efficiencyScore, progress.getDifficultyRating()
        );

        return ProgressAnalysis.builder()
                .achievementRate(achievementRate)
                .efficiencyScore(efficiencyScore)
                .analysisDescription(analysisDescription)
                .recommendation(recommendation)
                .build();
    }

    private static String generateAnalysisDescription(double achievementRate, int efficiencyScore, Integer difficultyRating) {
        StringBuilder analysis = new StringBuilder("운동 수행 분석: ");

        analysis.append(String.format("목표 대비 %.1f%% 달성했으며, ", achievementRate));
        analysis.append(String.format("전체적인 운동 효율성은 %d점입니다. ", efficiencyScore));

        if (difficultyRating != null) {
            analysis.append("체감 난이도는 ").append(difficultyRating).append("점으로, ");
            if (difficultyRating >= 4) {
                analysis.append("다소 높은 강도로 운동했습니다.");
            } else if (difficultyRating <= 2) {
                analysis.append("비교적 수월하게 운동했습니다.");
            } else {
                analysis.append("적절한 강도로 운동했습니다.");
            }
        }

        return analysis.toString();
    }

    private static String generateRecommendation(double achievementRate, int efficiencyScore, Integer difficultyRating) {
        StringBuilder recommendation = new StringBuilder("향후 운동 추천: ");

        if (achievementRate < 80) {
            recommendation.append("목표 달성률이 다소 낮으므로, 운동 시간을 조금씩 늘려보세요. ");
        } else if (achievementRate > 120) {
            recommendation.append("목표를 크게 초과 달성했습니다. 운동 강도나 목표를 상향 조정해보세요. ");
        }

        if (difficultyRating != null) {
            if (difficultyRating >= 4 && achievementRate < 90) {
                recommendation.append("운동 강도가 다소 높은 것 같습니다. 강도를 낮추고 시간을 늘려보세요. ");
            } else if (difficultyRating <= 2 && achievementRate > 100) {
                recommendation.append("운동 강도를 높여 더 효율적인 운동을 해보세요. ");
            }
        }

        if (efficiencyScore < 60) {
            recommendation.append("전반적인 운동 효율성 향상이 필요합니다. 운동 강도와 시간을 재조정해보세요.");
        }

        return recommendation.toString();
    }
}