// ExercisePreference.java
package com.fitter.domain.exercise;

import com.fitter.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "exercise_preferences")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExercisePreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_body_part")
    private ExerciseBase.BodyPart preferredBodyPart;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_category")
    private ExerciseBase.ExerciseCategory preferredCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_intensity")
    private ExerciseRoutine.IntensityLevel preferredIntensity;

    @Column(name = "preferred_time_of_day")
    private LocalTime preferredTimeOfDay;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 선호도 업데이트 메서드들
    public void updatePreferredBodyPart(ExerciseBase.BodyPart bodyPart) {
        this.preferredBodyPart = bodyPart;
    }

    public void updatePreferredCategory(ExerciseBase.ExerciseCategory category) {
        this.preferredCategory = category;
    }

    public void updatePreferredIntensity(ExerciseRoutine.IntensityLevel intensity) {
        this.preferredIntensity = intensity;
    }

    public void updatePreferredTimeOfDay(LocalTime timeOfDay) {
        this.preferredTimeOfDay = timeOfDay;
    }

    // 운동 추천 시 선호도 점수 계산
    public double calculatePreferenceScore(ExerciseBase exercise, ExerciseRoutine.IntensityLevel intensity, LocalTime time) {
        double score = 0.0;

        // 선호하는 신체 부위 매칭
        if (preferredBodyPart != null && preferredBodyPart == exercise.getBodyPart()) {
            score += 0.3; // 30% 가중치
        }

        // 선호하는 운동 카테고리 매칭
        if (preferredCategory != null && preferredCategory == exercise.getExerciseCategory()) {
            score += 0.3; // 30% 가중치
        }

        // 선호하는 운동 강도 매칭
        if (preferredIntensity != null && preferredIntensity == intensity) {
            score += 0.2; // 20% 가중치
        }

        // 선호하는 시간대 매칭 (1시간 이내 차이를 허용)
        if (preferredTimeOfDay != null && time != null) {
            long timeDiff = Math.abs(preferredTimeOfDay.toSecondOfDay() - time.toSecondOfDay());
            if (timeDiff <= 3600) { // 1시간 = 3600초
                score += 0.2; // 20% 가중치
            }
        }

        return score;
    }
}