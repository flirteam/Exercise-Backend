// LoginResponse.java
package com.fitter.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;     // JWT 액세스 토큰
    private String refreshToken;    // JWT 리프레시 토큰
    private UserResponse userInfo;  // 로그인한 사용자 정보

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;            // 사용자 ID
        private String email;       // 이메일
        private String username;    // 사용자명
        private PhysicalInfoResponse physicalInfo;  // 신체 정보
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhysicalInfoResponse {
        private Double currentWeight;          // 현재 체중
        private Double targetWeight;           // 목표 체중
        private Double height;                 // 키
        private Integer age;                   // 나이
        private String gender;                 // 성별
        private Integer activityLevel;         // 활동 레벨
        private String goalType;               // 목표 유형
        private Double basalMetabolicRate;     // 기초대사량
        private Double activeMetabolicRate;    // 활성대사량
        private Double bmi;                    // BMI
        private Double bodyFatPercentage;      // 체지방률
    }
}