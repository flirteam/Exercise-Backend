// ExercisePreferenceRepository.java
package com.fitter.repository.exercise;

import com.fitter.domain.exercise.ExercisePreference;
import com.fitter.domain.exercise.ExerciseBase;
import com.fitter.domain.exercise.ExerciseRoutine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExercisePreferenceRepository extends JpaRepository<ExercisePreference, Long> {

    // 사용자 선호도 조회
    Optional<ExercisePreference> findByUserId(Long userId);

    // 특정 신체 부위 선호 사용자 조회
    List<ExercisePreference> findByPreferredBodyPart(ExerciseBase.BodyPart bodyPart);

    // 특정 운동 카테고리 선호 사용자 조회
    List<ExercisePreference> findByPreferredCategory(ExerciseBase.ExerciseCategory category);

    // 특정 강도 선호 사용자 조회
    List<ExercisePreference> findByPreferredIntensity(ExerciseRoutine.IntensityLevel intensity);

    // 선호 시간대별 사용자 분포
    @Query("""
        SELECT EXTRACT(HOUR FROM ep.preferredTimeOfDay) as hour,
        COUNT(ep) as count
        FROM ExercisePreference ep
        WHERE ep.preferredTimeOfDay IS NOT NULL
        GROUP BY EXTRACT(HOUR FROM ep.preferredTimeOfDay)
        ORDER BY EXTRACT(HOUR FROM ep.preferredTimeOfDay)
    """)
    List<Object[]> getPreferredTimeDistribution();

    // 사용자 그룹별 선호도 통계
    @Query("""
        SELECT 
            CASE 
                WHEN u.physicalInfo.age < 30 THEN 'YOUNG'
                WHEN u.physicalInfo.age < 50 THEN 'MIDDLE'
                ELSE 'SENIOR'
            END as ageGroup,
            ep.preferredBodyPart as bodyPart,
            COUNT(ep) as count
        FROM ExercisePreference ep
        JOIN ep.user u
        GROUP BY 
            CASE 
                WHEN u.physicalInfo.age < 30 THEN 'YOUNG'
                WHEN u.physicalInfo.age < 50 THEN 'MIDDLE'
                ELSE 'SENIOR'
            END,
            ep.preferredBodyPart
    """)
    List<Object[]> getPreferenceStatisticsByAgeGroup();

    // BMI 구간별 선호도 통계
    @Query("""
        SELECT 
            CASE 
                WHEN u.physicalInfo.bmi < 18.5 THEN 'UNDERWEIGHT'
                WHEN u.physicalInfo.bmi < 25 THEN 'NORMAL'
                WHEN u.physicalInfo.bmi < 30 THEN 'OVERWEIGHT'
                ELSE 'OBESE'
            END as bmiCategory,
            ep.preferredIntensity as intensity,
            COUNT(ep) as count
        FROM ExercisePreference ep
        JOIN ep.user u
        GROUP BY 
            CASE 
                WHEN u.physicalInfo.bmi < 18.5 THEN 'UNDERWEIGHT'
                WHEN u.physicalInfo.bmi < 25 THEN 'NORMAL'
                WHEN u.physicalInfo.bmi < 30 THEN 'OVERWEIGHT'
                ELSE 'OBESE'
            END,
            ep.preferredIntensity
    """)
    List<Object[]> getPreferenceStatisticsByBmiCategory();

    // 특정 시간대 선호 사용자 조회
    @Query("""
        SELECT ep FROM ExercisePreference ep
        WHERE EXTRACT(HOUR FROM ep.preferredTimeOfDay) = EXTRACT(HOUR FROM :time)
    """)
    List<ExercisePreference> findByPreferredHour(@Param("time") LocalTime time);
}