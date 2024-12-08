// UserPhysicalInfo.java
package com.fitter.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_physical_info")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPhysicalInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "current_weight", nullable = false)
    private Double currentWeight;

    @Column(name = "target_weight", nullable = false)
    private Double targetWeight;

    @Column(nullable = false)
    private Double height;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(name = "activity_level", nullable = false)
    private Integer activityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false)
    private GoalType goalType;

    @Column(name = "basal_metabolic_rate")
    private Double basalMetabolicRate;  // BMR

    @Column(name = "active_metabolic_rate")
    private Double activeMetabolicRate;  // AMR

    @Column(name = "bmi")
    private Double bmi;

    @Column(name = "body_fat_percentage")
    private Double bodyFatPercentage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void setUser(User user) {
        this.user = user;
    }

    // UserPhysicalInfo.java의 updatePhysicalInfo 메소드를 수정
    public void updatePhysicalInfo(
            Integer age,
            Double height,
            Double currentWeight,
            Integer activityLevel,
            Double targetWeight,
            Gender gender,
            GoalType goalType) {
        this.age = age;
        this.height = height;
        this.currentWeight = currentWeight;
        this.activityLevel = activityLevel;
        this.targetWeight = targetWeight;
        this.gender = gender;
        this.goalType = goalType;
        calculateMetabolicRates();
    }

    @PrePersist
    @PreUpdate
    public void calculateMetabolicRates() {
        // 기본값이 0인 경우 메타볼릭 계산 생략
        if (currentWeight == 0 || height == 0 || age == 0) {
            this.basalMetabolicRate = 0.0;
            this.activeMetabolicRate = 0.0;
            this.bmi = 0.0;
            this.bodyFatPercentage = 0.0;
            return;
        }

        // BMR 계산 (Mifflin-St Jeor 공식)
        if (gender == Gender.Male) {
            this.basalMetabolicRate = (10 * currentWeight) + (6.25 * height) - (5 * age) + 5;
        } else {
            this.basalMetabolicRate = (10 * currentWeight) + (6.25 * height) - (5 * age) - 161;
        }

        // AMR 계산
        double activityFactor = switch (activityLevel) {
            case 1 -> 1.725;
            case 2 -> 1.55;
            case 3 -> 1.375;
            case 4 -> 1.2;
            default -> 1.2;
        };
        this.activeMetabolicRate = this.basalMetabolicRate * activityFactor;

        // BMI 계산
        this.bmi = currentWeight / ((height/100) * (height/100));

        // 체지방률 계산 (BMI 방법 - 추정치)
        this.bodyFatPercentage = (1.2 * bmi) + (0.23 * age) -
                (10.8 * (gender == Gender.Male ? 1 : 0)) - 5.4;
    }

    public enum Gender {
        Male, Female
    }

    public enum GoalType {
        저지방_고단백("저지방_고단백"),
        균형_식단("균형_식단"),
        벌크업("벌크업");

        private final String value;

        GoalType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}