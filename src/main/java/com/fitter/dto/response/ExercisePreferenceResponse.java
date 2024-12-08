// ExercisePreferenceResponse.java
package com.fitter.dto.response;

import com.fitter.domain.exercise.ExercisePreference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExercisePreferenceResponse {
    private Long id;
    private String preferredBodyPart;
    private String preferredCategory;
    private String preferredIntensity;
    private LocalTime preferredTimeOfDay;

    public static ExercisePreferenceResponse from(ExercisePreference preference) {
        return ExercisePreferenceResponse.builder()
                .id(preference.getId())
                .preferredBodyPart(preference.getPreferredBodyPart().name())
                .preferredCategory(preference.getPreferredCategory().name())
                .preferredIntensity(preference.getPreferredIntensity().name())
                .preferredTimeOfDay(preference.getPreferredTimeOfDay())
                .build();
    }
}