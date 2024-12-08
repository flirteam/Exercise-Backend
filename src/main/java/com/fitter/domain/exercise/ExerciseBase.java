// ExerciseBase.java
package com.fitter.domain.exercise;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercise_base", indexes = {
        @Index(name = "idx_name", columnList = "name"),
        @Index(name = "idx_exercise_category", columnList = "exercise_category"),
        @Index(name = "idx_body_part", columnList = "body_part")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BodyPart bodyPart;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExerciseCategory exerciseCategory;

    @Column(name = "base_calories_burned", nullable = false)
    private Double baseCaloriesBurned;  // 기준 체중(70kg)에서의 분당 칼로리 소모량

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "media_url")
    private String mediaUrl;

    @OneToMany(mappedBy = "exerciseBase", cascade = CascadeType.ALL)
    private List<ExerciseRoutine> exerciseRoutines = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum BodyPart {
        등, 하체, 이두, 스포츠, 가슴, 코어, 전완근, 어깨, 삼두, 유산소
    }

    public enum ExerciseCategory {
        전신강화운동("전신강화운동"),
        상체근력운동("상체근력운동"),
        하체근력운동("하체근력운동"),
        유산소운동("유산소운동"),
        코어강화운동("코어강화운동"),
        유연성및스트레칭운동("유연성및스트레칭운동");

        private final String value;

        ExerciseCategory(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // 실제 칼로리 소모량 계산 메서드
    public double calculateActualCaloriesBurned(double userWeight, int durationMinutes) {
        // 사용자의 체중에 따른 보정 계수 (기준 체중 70kg)
        double weightFactor = userWeight / 70.0;
        return baseCaloriesBurned * weightFactor * durationMinutes;
    }

}