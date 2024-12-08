// PhysicalInfoRequest.java
package com.fitter.dto.request;

import com.fitter.domain.user.UserPhysicalInfo;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalInfoRequest {
    @NotNull(message = "현재 체중은 필수 입력값입니다.")
    @DecimalMin(value = "30.0", message = "현재 체중은 30kg 이상이어야 합니다.")
    @DecimalMax(value = "300.0", message = "현재 체중은 300kg 이하여야 합니다.")
    private Double currentWeight;

    @NotNull(message = "목표 체중은 필수 입력값입니다.")
    @DecimalMin(value = "30.0", message = "목표 체중은 30kg 이상이어야 합니다.")
    @DecimalMax(value = "300.0", message = "목표 체중은 300kg 이하여야 합니다.")
    private Double targetWeight;

    @NotNull(message = "신장은 필수 입력값입니다.")
    @DecimalMin(value = "100.0", message = "신장은 100cm 이상이어야 합니다.")
    @DecimalMax(value = "250.0", message = "신장은 250cm 이하여야 합니다.")
    private Double height;

    @NotNull(message = "나이는 필수 입력값입니다.")
    @Min(value = 14, message = "나이는 14세 이상이어야 합니다.")
    @Max(value = 100, message = "나이는 100세 이하여야 합니다.")
    private Integer age;

    @NotNull(message = "성별은 필수 입력값입니다.")
    private UserPhysicalInfo.Gender gender;

    @NotNull(message = "활동량 계수는 필수 입력값입니다.")
    @Min(value = 1, message = "활동량 계수는 1 이상이어야 합니다.")
    @Max(value = 4, message = "활동량 계수는 4 이하여야 합니다.")
    private Integer activityLevel;

    @NotNull(message = "목표 식단 유형은 필수 입력값입니다.")
    private UserPhysicalInfo.GoalType goalType;
}