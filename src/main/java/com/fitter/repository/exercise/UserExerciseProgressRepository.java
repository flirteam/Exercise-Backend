// UserExerciseProgressRepository.java
package com.fitter.repository.exercise;

import com.fitter.domain.exercise.UserExerciseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface UserExerciseProgressRepository extends JpaRepository<UserExerciseProgress, Long> {

    // 사용자별 운동 진행상황 조회
    List<UserExerciseProgress> findByUserIdOrderByCompletedDateDesc(Long userId);

    // 기간별 운동 진행상황 조회
    List<UserExerciseProgress> findByUserIdAndCompletedDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    // 특정 날짜의 운동 진행상황 조회
    List<UserExerciseProgress> findByUserIdAndCompletedDate(Long userId, LocalDate date);

    // 특정 기간 이후의 운동 진행상황 조회
    List<UserExerciseProgress> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime dateTime);

    // 난이도별 운동 수행 통계
    @Query("""
        SELECT uep.exerciseRoutine.intensityLevel as intensity,
        COUNT(uep) as count,
        AVG(uep.difficultyRating) as avgDifficulty,
        AVG(uep.actualCaloriesBurned) as avgCalories
        FROM UserExerciseProgress uep
        WHERE uep.user.id = :userId
        AND uep.completedDate BETWEEN :startDate AND :endDate
        GROUP BY uep.exerciseRoutine.intensityLevel
    """)
    List<Object[]> getIntensityStatistics(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 운동 효율성 분석
    @Query("""
        SELECT new map(
            uep.completedDate as date,
            AVG(uep.actualCaloriesBurned / uep.actualDurationMinutes) as caloriesPerMinute,
            AVG(CAST(uep.actualCaloriesBurned as float) / 
                CAST(uep.exerciseRoutine.caloriesBurned as float) * 100) as achievementRate
        )
        FROM UserExerciseProgress uep
        WHERE uep.user.id = :userId
        AND uep.completedDate BETWEEN :startDate AND :endDate
        GROUP BY uep.completedDate
        ORDER BY uep.completedDate
    """)
    List<Map<String, Object>> analyzeExerciseEfficiency(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}