// ExerciseDifficultyMapRepository.java
package com.fitter.repository.exercise;

import com.fitter.domain.exercise.ExerciseDifficultyMap;
import com.fitter.domain.exercise.ExerciseRoutine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseDifficultyMapRepository extends JpaRepository<ExerciseDifficultyMap, Long> {

    // BMI와 나이에 맞는 운동 난이도 매핑 조회
    @Query("""
        SELECT edm FROM ExerciseDifficultyMap edm
        WHERE :bmi BETWEEN edm.bmiRangeStart AND edm.bmiRangeEnd
        AND :age BETWEEN edm.ageRangeStart AND edm.ageRangeEnd
        AND edm.activityLevel = :activityLevel
    """)
    Optional<ExerciseDifficultyMap> findMatchingDifficultyMap(
            @Param("bmi") Double bmi,
            @Param("age") Integer age,
            @Param("activityLevel") Integer activityLevel
    );

    // 특정 BMI 범위의 난이도 매핑 조회
    List<ExerciseDifficultyMap> findByBmiRangeStartLessThanEqualAndBmiRangeEndGreaterThanEqual(
            Double bmiEnd,
            Double bmiStart
    );

    // 연령대별 권장 운동 강도 조회
    @Query("""
        SELECT edm.ageRangeStart as ageStart,
        edm.ageRangeEnd as ageEnd,
        edm.recommendedIntensity as intensity,
        COUNT(edm) as count
        FROM ExerciseDifficultyMap edm
        GROUP BY edm.ageRangeStart, edm.ageRangeEnd, edm.recommendedIntensity
        ORDER BY edm.ageRangeStart
    """)
    List<Object[]> getRecommendedIntensitiesByAgeGroup();

    // BMI 구간별 권장 운동 강도 분포
    @Query("""
        SELECT edm.bmiRangeStart as bmiStart,
        edm.bmiRangeEnd as bmiEnd,
        edm.recommendedIntensity as intensity,
        COUNT(edm) as count
        FROM ExerciseDifficultyMap edm
        GROUP BY edm.bmiRangeStart, edm.bmiRangeEnd, edm.recommendedIntensity
        ORDER BY edm.bmiRangeStart
    """)
    List<Object[]> getRecommendedIntensitiesByBmiRange();
}