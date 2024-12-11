//ExerciseGoalResponse.java
package com.fitter.dto.response;

import com.fitter.domain.exercise.ExerciseGoal;
import com.fitter.service.exercise.ExerciseGoalService.GoalAchievementAnalysis;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseGoalResponse {
    private Long id;
    private String name;
    private Double targetCaloriesPerDay;
    private Integer targetExerciseMinutesPerDay;
    private LocalDate startDate;
    private LocalDate endDate;
    private ExerciseGoal.GoalStatus status;
    private AchievementAnalysis achievementAnalysis;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AchievementAnalysis {
        private double caloriesAchievementRate;
        private double minutesAchievementRate;
        private String feedback;
    }

    public static ExerciseGoalResponse from(ExerciseGoal goal, GoalAchievementAnalysis analysis) {
        return ExerciseGoalResponse.builder()
                .id(goal.getId())
                .name(goal.getName())
                .targetCaloriesPerDay(goal.getTargetCaloriesPerDay())
                .targetExerciseMinutesPerDay(goal.getTargetExerciseMinutesPerDay())
                .startDate(goal.getStartDate())
                .endDate(goal.getEndDate())
                .status(goal.getStatus())
                .achievementAnalysis(AchievementAnalysis.builder()
                        .caloriesAchievementRate(analysis.getCaloriesAchievementRate())
                        .minutesAchievementRate(analysis.getMinutesAchievementRate())
                        .feedback(analysis.getFeedback())
                        .build())
                .build();
    }
}