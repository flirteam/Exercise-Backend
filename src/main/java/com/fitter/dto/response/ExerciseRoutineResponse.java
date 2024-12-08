// ExerciseRoutineResponse.java
package com.fitter.dto.response;

import com.fitter.domain.exercise.ExerciseBase;
import com.fitter.domain.exercise.ExerciseRoutine;
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
public class ExerciseRoutineResponse {
    private Long id;
    private Long userId;
    private ExerciseBaseInfo exerciseBase;
    private Integer durationMinutes;
    private ExerciseRoutine.IntensityLevel intensityLevel;
    private Double caloriesBurned;
    private LocalDate routineDate;
    private Boolean completionStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExerciseBaseInfo {
        private Long id;
        private String name;
        private ExerciseBase.BodyPart bodyPart;
        private ExerciseBase.ExerciseCategory exerciseCategory;
        private String description;
        private String mediaUrl;
    }

    public static ExerciseRoutineResponse from(ExerciseRoutine routine) {
        return ExerciseRoutineResponse.builder()
                .id(routine.getId())
                .userId(routine.getUser().getId())
                .exerciseBase(ExerciseBaseInfo.builder()
                        .id(routine.getExerciseBase().getId())
                        .name(routine.getExerciseBase().getName())
                        .bodyPart(routine.getExerciseBase().getBodyPart())
                        .exerciseCategory(routine.getExerciseBase().getExerciseCategory())
                        .description(routine.getExerciseBase().getDescription())
                        .mediaUrl(routine.getExerciseBase().getMediaUrl())
                        .build())
                .durationMinutes(routine.getDurationMinutes())
                .intensityLevel(routine.getIntensityLevel())
                .caloriesBurned(routine.getCaloriesBurned())
                .routineDate(routine.getRoutineDate())
                .completionStatus(routine.getCompletionStatus())
                .createdAt(routine.getCreatedAt())
                .updatedAt(routine.getUpdatedAt())
                .build();
    }
}