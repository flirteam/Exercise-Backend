// UserResponse.java
package com.fitter.dto.response;

import com.fitter.domain.user.User;
import com.fitter.domain.user.UserPhysicalInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private PhysicalInfoResponse physicalInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhysicalInfoResponse {
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
    }

    public static UserResponse from(User user) {
        UserPhysicalInfo physicalInfo = user.getPhysicalInfo();

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .physicalInfo(physicalInfo != null ? PhysicalInfoResponse.builder()
                        .currentWeight(physicalInfo.getCurrentWeight())
                        .targetWeight(physicalInfo.getTargetWeight())
                        .height(physicalInfo.getHeight())
                        .age(physicalInfo.getAge())
                        .gender(physicalInfo.getGender().name())
                        .activityLevel(physicalInfo.getActivityLevel())
                        .goalType(physicalInfo.getGoalType() != null ?
                                physicalInfo.getGoalType().name() : null)  // 여기를 수정
                        .basalMetabolicRate(physicalInfo.getBasalMetabolicRate())
                        .activeMetabolicRate(physicalInfo.getActiveMetabolicRate())
                        .bmi(physicalInfo.getBmi())
                        .bodyFatPercentage(physicalInfo.getBodyFatPercentage())
                        .build() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}