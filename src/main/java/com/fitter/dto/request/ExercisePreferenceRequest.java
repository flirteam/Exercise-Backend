// ExercisePreferenceRequest.java
package com.fitter.dto.request;

import com.fitter.domain.exercise.ExerciseBase;
import com.fitter.domain.exercise.ExerciseRoutine;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExercisePreferenceRequest {

    @NotNull(message = "선호하는 신체 부위는 필수입니다.")
    private ExerciseBase.BodyPart preferredBodyPart;

    //@NotNull(message = "선호하는 운동 카테고리는 필수입니다.")
    //private ExerciseBase.ExerciseCategory preferredCategory;

    @NotNull(message = "선호하는 운동 강도는 필수입니다.")
    private ExerciseRoutine.IntensityLevel preferredIntensity;

    @NotNull(message = "선호하는 운동 시간은 필수입니다.")
    private LocalTime preferredTimeOfDay;


    // 선호 시간대 유효성 검사
    public boolean isValidPreferredTime() {
        if (preferredTimeOfDay == null) {
            return true;
        }

        // 새벽 4시부터 밤 11시까지만 허용
        LocalTime minTime = LocalTime.of(4, 0);
        LocalTime maxTime = LocalTime.of(23, 0);

        return !preferredTimeOfDay.isBefore(minTime) && !preferredTimeOfDay.isAfter(maxTime);
    }

    // 최소한 하나의 선호도는 설정되어야 함
    public boolean hasAtLeastOnePreference() {
        return preferredBodyPart != null ||
                //preferredCategory != null ||
                preferredIntensity != null ||
                preferredTimeOfDay != null;
    }

    // BMI와 활동량에 따른 선호도 유효성 검사
    public boolean isValidPreferenceForUserProfile(double bmi, int activityLevel) {
        if (preferredIntensity == null) {
            return true;
        }

        // BMI와 활동량에 따른 강도 제한
        if (bmi >= 30 || activityLevel >= 3) {
            return preferredIntensity != ExerciseRoutine.IntensityLevel.높음;
        }

        if (bmi <= 18.5 || activityLevel == 4) {
            return preferredIntensity != ExerciseRoutine.IntensityLevel.높음;
        }

        return true;
    }
}