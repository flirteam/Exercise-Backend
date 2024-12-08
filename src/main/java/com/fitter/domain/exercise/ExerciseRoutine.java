// ExerciseRoutine.java
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
@Table(name = "exercise_routine")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseRoutine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_base_id", nullable = false)
    private ExerciseBase exerciseBase;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "intensity_level", nullable = false)
    private IntensityLevel intensityLevel;

    @Column(name = "calories_burned", nullable = false)
    private Double caloriesBurned;

    @Column(name = "routine_date", nullable = false)
    private LocalDate routineDate;

    @Column(name = "completion_status")
    @Builder.Default
    private Boolean completionStatus = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum IntensityLevel {
        낮음("낮음", 0.8),
        중간("중간", 1.0),
        높음("높음", 1.2);

        private final String value;
        private final double factor;

        IntensityLevel(String value, double factor) {
            this.value = value;
            this.factor = factor;
        }

        public String getValue() {
            return value;
        }

        public double getFactor() {
            return factor;
        }
    }

    // 운동 완료 상태 변경
    public void completeExercise() {
        this.completionStatus = true;
    }

    // 운동 시간 업데이트
    public void updateDuration(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
        updateCaloriesBurned(caloriesBurned);
    }

    // 운동 강도 업데이트
    public void updateIntensity(IntensityLevel intensityLevel) {
        this.intensityLevel = intensityLevel;
        updateCaloriesBurned(caloriesBurned);
    }

    public void updateCaloriesBurned(double newCaloriesBurned) {
        this.caloriesBurned = newCaloriesBurned;
    }

    public void recalculateCalories() {
        double baseCalories = exerciseBase.calculateActualCaloriesBurned(
                user.getPhysicalInfo().getCurrentWeight(),
                this.durationMinutes
        );
        this.caloriesBurned = baseCalories * intensityLevel.getFactor();
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setIntensityLevel(IntensityLevel intensityLevel) {
        this.intensityLevel = intensityLevel;
    }

    public void setRoutineDate(LocalDate routineDate) {
        this.routineDate = routineDate;
    }

    public void setCaloriesBurned(Double caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }
}