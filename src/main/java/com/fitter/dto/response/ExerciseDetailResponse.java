//ExerciseDetailResponse.java
package com.fitter.dto.response;

import com.fitter.domain.exercise.ExerciseBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseDetailResponse {
    private Long id;
    private String name;
    private String bodyPart;
    private String exerciseCategory;
    private String description;
    private String mediaUrl;
    private Double baseCaloriesBurned;

    public static ExerciseDetailResponse from(ExerciseBase exercise) {
        if (exercise == null) {
            return null;
        }

        return ExerciseDetailResponse.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .bodyPart(exercise.getBodyPart().name())
                .exerciseCategory(exercise.getExerciseCategory().name())
                .description(exercise.getDescription())
                .mediaUrl(exercise.getMediaUrl())
                .baseCaloriesBurned(exercise.getBaseCaloriesBurned())
                .build();
    }
}