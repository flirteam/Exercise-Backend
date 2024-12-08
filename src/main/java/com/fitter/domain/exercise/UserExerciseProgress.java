// UserExerciseProgress.java
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
@Table(name = "user_exercise_progress")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserExerciseProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_routine_id", nullable = false)
    private ExerciseRoutine exerciseRoutine;

    @Column(name = "completed_date", nullable = false)
    private LocalDate completedDate;

    @Column(name = "actual_duration_minutes", nullable = false)
    private Integer actualDurationMinutes;

    @Column(name = "actual_calories_burned", nullable = false)
    private Double actualCaloriesBurned;

    @Column(name = "difficulty_rating")
    private Integer difficultyRating; // 1-5 scale

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 운동 난이도 평가 및 피드백 추가
    public void addFeedback(int difficultyRating, String feedback) {
        this.difficultyRating = difficultyRating;
        this.feedback = feedback;
    }

    // 실제 운동 시간과 소모 칼로리 업데이트
    public void updateProgress(int actualDurationMinutes) {
        this.actualDurationMinutes = actualDurationMinutes;
        updateActualCaloriesBurned();
    }

    // 실제 소모 칼로리 계산
    private void updateActualCaloriesBurned() {
        double baseCalories = exerciseRoutine.getExerciseBase().calculateActualCaloriesBurned(
                user.getPhysicalInfo().getCurrentWeight(),
                this.actualDurationMinutes
        );

        // 운동 강도 반영
        this.actualCaloriesBurned = baseCalories * exerciseRoutine.getIntensityLevel().getFactor();
    }

    // 목표 대비 달성률 계산 (%)
    public double calculateAchievementRate() {
        double targetCalories = exerciseRoutine.getCaloriesBurned();
        return (actualCaloriesBurned / targetCalories) * 100;
    }

    // 운동 효율성 점수 계산 (1-100)
    public int calculateEfficiencyScore() {
        // 기본 점수 (달성률 기반)
        double achievementScore = calculateAchievementRate();

        // 난이도 평가 반영 (최대 20점)
        double difficultyScore = (difficultyRating != null) ?
                (20.0 * (5 - difficultyRating) / 4) : 0;

        // 시간 효율성 반영 (최대 20점)
        double targetDuration = exerciseRoutine.getDurationMinutes();
        double timeEfficiencyScore = 20.0 * (1 - Math.abs(actualDurationMinutes - targetDuration) / targetDuration);

        return (int) Math.min(Math.max(achievementScore + difficultyScore + timeEfficiencyScore, 0), 100);
    }
}