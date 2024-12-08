// ExerciseDifficultyMap.java
package com.fitter.domain.exercise;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_difficulty_mapping")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseDifficultyMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bmi_range_start")
    private Double bmiRangeStart;

    @Column(name = "bmi_range_end")
    private Double bmiRangeEnd;

    @Column(name = "age_range_start")
    private Integer ageRangeStart;

    @Column(name = "age_range_end")
    private Integer ageRangeEnd;

    @Column(name = "activity_level")
    private Integer activityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_intensity")
    private ExerciseRoutine.IntensityLevel recommendedIntensity;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // BMI, 나이, 활동 레벨이 현재 맵핑에 해당하는지 확인
    public boolean isApplicable(double bmi, int age, int activityLevel) {
        return bmi >= bmiRangeStart && bmi <= bmiRangeEnd &&
                age >= ageRangeStart && age <= ageRangeEnd &&
                this.activityLevel.equals(activityLevel);
    }

    // 운동 강도 추천 로직
    public ExerciseRoutine.IntensityLevel recommendIntensity(double bmi, int age, int activityLevel) {
        if (!isApplicable(bmi, age, activityLevel)) {
            return ExerciseRoutine.IntensityLevel.중간; // 기본값
        }
        return recommendedIntensity;
    }
}
