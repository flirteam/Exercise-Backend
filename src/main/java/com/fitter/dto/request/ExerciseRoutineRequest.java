// ExerciseRoutineRequest.java
package com.fitter.dto.request;

import com.fitter.domain.exercise.ExerciseRoutine;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseRoutineRequest {

    @NotNull(message = "운동 기본 정보 ID는 필수입니다.")
    private Long exerciseBaseId;

    @NotNull(message = "운동 시간은 필수입니다.")
    @Min(value = 1, message = "운동 시간은 1분 이상이어야 합니다.")
    private Integer durationMinutes;

    @NotNull(message = "운동 강도는 필수입니다.")
    private ExerciseRoutine.IntensityLevel intensityLevel;

    @NotNull(message = "운동 날짜는 필수입니다.")
    private LocalDate routineDate;

    // 운동 완료 여부 (선택)
    private Boolean completionStatus;

    // 유효성 검사를 위한 커스텀 메서드
    public boolean isValidRoutineDate() {
        return routineDate != null &&
                !routineDate.isBefore(LocalDate.now().minusDays(7)) && // 일주일 전까지만 허용
                !routineDate.isAfter(LocalDate.now().plusMonths(1));   // 한 달 후까지만 허용
    }
}