//UserPhysicalInfoResponse.java
package com.fitter.dto.response;

import com.fitter.domain.user.UserPhysicalInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPhysicalInfoResponse {
    private Double currentWeight;
    private Double targetWeight;
    private Double height;
    private Integer age;
    private String gender;
    private Integer activityLevel;
    private String goalType;
    private Double basalMetabolicRate;
    private Double activeMetabolicRate;
    private Double bmi;
    private Double bodyFatPercentage;

    public static UserPhysicalInfoResponse from(UserPhysicalInfo info) {
        return UserPhysicalInfoResponse.builder()
                .currentWeight(info.getCurrentWeight())
                .targetWeight(info.getTargetWeight())
                .height(info.getHeight())
                .age(info.getAge())
                .gender(info.getGender().name())
                .activityLevel(info.getActivityLevel())
                .goalType(info.getGoalType().name())
                .basalMetabolicRate(info.getBasalMetabolicRate())
                .activeMetabolicRate(info.getActiveMetabolicRate())
                .bmi(info.getBmi())
                .bodyFatPercentage(info.getBodyFatPercentage())
                .build();
    }
}