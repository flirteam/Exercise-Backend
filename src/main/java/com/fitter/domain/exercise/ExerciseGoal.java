//ExerciseGoal.java
package com.fitter.domain.exercise;

import com.fitter.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_goals")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "target_calories_per_day", nullable = false)
    private Double targetCaloriesPerDay;

    @Column(name = "target_exercise_minutes_per_day", nullable = false)
    private Integer targetExerciseMinutesPerDay;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private GoalStatus status = GoalStatus.진행중;

    @Column(name = "calories_achievement_rate")
    private Double caloriesAchievementRate;

    @Column(name = "minutes_achievement_rate")
    private Double minutesAchievementRate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    public enum GoalStatus {
        진행중, 완료, 중단
    }

    // 목표 상태 업데이트
    public void updateStatus(GoalStatus status) {
        this.status = status;
        if (status == GoalStatus.완료 || status == GoalStatus.중단) {
            this.endDate = LocalDate.now();
        }
    }

    // 목표 달성 여부 확인
    public boolean isAchieved(double currentCaloriesBurned, int currentExerciseMinutes) {
        return currentCaloriesBurned >= targetCaloriesPerDay &&
                currentExerciseMinutes >= targetExerciseMinutesPerDay;
    }

    // 목표 달성률 계산
    public double calculateAchievementRate(double currentCaloriesBurned, int currentExerciseMinutes) {
        double caloriesRate = (currentCaloriesBurned / targetCaloriesPerDay) * 100;
        double minutesRate = (currentExerciseMinutes / (double) targetExerciseMinutesPerDay) * 100;
        return (caloriesRate + minutesRate) / 2;
    }

    // 달성률 업데이트
    public void updateAchievementRates(double caloriesRate, double minutesRate) {
        this.caloriesAchievementRate = caloriesRate;
        this.minutesAchievementRate = minutesRate;
    }

    public void updateGoal(String name, Double targetCaloriesPerDay, Integer targetExerciseMinutesPerDay,
                           LocalDate startDate, LocalDate endDate) {
        if (name != null) {
            this.name = name;
        }
        if (targetCaloriesPerDay != null) {
            this.targetCaloriesPerDay = targetCaloriesPerDay;
        }
        if (targetExerciseMinutesPerDay != null) {
            this.targetExerciseMinutesPerDay = targetExerciseMinutesPerDay;
        }
        if (startDate != null) {
            this.startDate = startDate;
        }
        if (endDate != null) {
            this.endDate = endDate;
        }
    }

}