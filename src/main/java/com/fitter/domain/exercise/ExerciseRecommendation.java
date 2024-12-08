// ExerciseRecommendation.java
package com.fitter.domain.exercise;

import com.fitter.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_recommendation_history")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_routine_id", nullable = false)
    private ExerciseRoutine exerciseRoutine;

    @Column(name = "recommendation_date", nullable = false)
    private LocalDate recommendationDate;

    @Column(name = "calories_burned", nullable = false)
    private Double caloriesBurned;

    @Column(name = "completion_status")
    @Builder.Default
    private Boolean completionStatus = false;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 추천 운동 완료 처리
    public void completeRecommendation(String feedback) {
        this.completionStatus = true;
        this.feedback = feedback;
    }

    // 추천 운동의 예상 소모 칼로리 업데이트
    public void updateCaloriesBurned(Double caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    // 사용자의 BMI와 활동량에 따른 운동 강도 결정
    public ExerciseRoutine.IntensityLevel determineIntensityLevel() {
        double bmi = user.getPhysicalInfo().getBmi();
        int activityLevel = user.getPhysicalInfo().getActivityLevel();

        if (bmi >= 30 || activityLevel >= 3) {
            return ExerciseRoutine.IntensityLevel.낮음;
        } else if (bmi >= 25 || activityLevel == 2) {
            return ExerciseRoutine.IntensityLevel.중간;
        } else {
            return ExerciseRoutine.IntensityLevel.높음;
        }
    }

    // 사용자의 목표 체중과 현재 체중을 고려한 권장 운동 시간 계산
    public int calculateRecommendedDuration() {
        double currentWeight = user.getPhysicalInfo().getCurrentWeight();
        double targetWeight = user.getPhysicalInfo().getTargetWeight();
        double weightDiff = Math.abs(currentWeight - targetWeight);

        // 기본 30분에 체중 차이에 따른 추가 시간
        int baseDuration = 30;
        int additionalMinutes = (int) (weightDiff * 2); // 체중 차이 1kg당 2분 추가

        return Math.min(baseDuration + additionalMinutes, 90); // 최대 90분으로 제한
    }
}