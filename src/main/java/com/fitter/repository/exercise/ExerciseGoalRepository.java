// ExerciseGoalRepository.java
package com.fitter.repository.exercise;

import com.fitter.domain.exercise.ExerciseGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ExerciseGoalRepository extends JpaRepository<ExerciseGoal, Long> {

    // 사용자의 현재 활성화된 목표 조회
    List<ExerciseGoal> findByUserIdAndStatus(Long userId, ExerciseGoal.GoalStatus status);

    // 특정 기간의 목표 조회
    @Query("""
        SELECT eg FROM ExerciseGoal eg
        WHERE eg.user.id = :userId
        AND eg.startDate <= :endDate
        AND (eg.endDate IS NULL OR eg.endDate >= :startDate)
        ORDER BY eg.startDate DESC
    """)
    List<ExerciseGoal> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 가장 최근 목표 조회
    Optional<ExerciseGoal> findFirstByUserIdOrderByStartDateDesc(Long userId);

    // 목표 달성률 통계
    @Query("""
        SELECT new map(
            eg.status as status,
            COUNT(eg) as count,
            AVG(CASE 
                WHEN eg.endDate IS NOT NULL 
                THEN DATEDIFF(eg.endDate, eg.startDate)
                ELSE DATEDIFF(CURRENT_DATE, eg.startDate)
            END) as avgDuration
        )
        FROM ExerciseGoal eg
        WHERE eg.user.id = :userId
        GROUP BY eg.status
    """)
    List<Map<String, Object>> getGoalStatistics(@Param("userId") Long userId);

    // 기간별 목표 달성 추이
    @Query("""
        SELECT eg.startDate as startDate,
        eg.targetCaloriesPerDay as targetCalories,
        eg.targetExerciseMinutesPerDay as targetMinutes,
        eg.status as status
        FROM ExerciseGoal eg
        WHERE eg.user.id = :userId
        AND eg.startDate BETWEEN :startDate AND :endDate
        ORDER BY eg.startDate
    """)
    List<Object[]> getGoalTrends(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 목표 유형별 성공률
    @Query("""
        SELECT 
            CASE 
                WHEN eg.targetCaloriesPerDay <= 500 THEN 'LOW'
                WHEN eg.targetCaloriesPerDay <= 800 THEN 'MEDIUM'
                ELSE 'HIGH'
            END as intensity,
            COUNT(eg) as total,
            SUM(CASE WHEN eg.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed
        FROM ExerciseGoal eg
        WHERE eg.user.id = :userId
        GROUP BY 
            CASE 
                WHEN eg.targetCaloriesPerDay <= 500 THEN 'LOW'
                WHEN eg.targetCaloriesPerDay <= 800 THEN 'MEDIUM'
                ELSE 'HIGH'
            END
    """)
    List<Object[]> getGoalSuccessRateByIntensity(@Param("userId") Long userId);
}