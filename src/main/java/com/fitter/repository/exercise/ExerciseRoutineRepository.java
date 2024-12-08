// ExerciseRoutineRepository.java
package com.fitter.repository.exercise;

import com.fitter.domain.exercise.ExerciseRoutine;
import com.fitter.domain.exercise.ExerciseBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ExerciseRoutineRepository extends JpaRepository<ExerciseRoutine, Long> {

    // 사용자별 운동 루틴 조회
    List<ExerciseRoutine> findByUserId(Long userId);

    // 사용자별 특정 기간 운동 루틴 조회
    List<ExerciseRoutine> findByUserIdAndRoutineDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    // 완료된 운동 루틴만 조회
    List<ExerciseRoutine> findByUserIdAndCompletionStatusTrue(Long userId);

    // 특정 운동의 최근 루틴 조회
    Optional<ExerciseRoutine> findFirstByUserIdAndExerciseBaseOrderByRoutineDateDesc(
            Long userId,
            ExerciseBase exerciseBase
    );

    // 사용자별 운동 통계 조회
    @Query("""
        SELECT new map(
            COUNT(r) as totalRoutines,
            SUM(CASE WHEN r.completionStatus = true THEN 1 ELSE 0 END) as completedRoutines,
            AVG(r.caloriesBurned) as avgCaloriesBurned,
            AVG(r.durationMinutes) as avgDuration
        )
        FROM ExerciseRoutine r
        WHERE r.user.id = :userId
        AND r.routineDate BETWEEN :startDate AND :endDate
    """)
    Map<String, Object> getUserStatistics(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 운동 강도별 완료율 조회
    @Query("""
        SELECT r.intensityLevel as intensity,
        COUNT(r) as total,
        SUM(CASE WHEN r.completionStatus = true THEN 1 ELSE 0 END) as completed
        FROM ExerciseRoutine r
        WHERE r.user.id = :userId
        GROUP BY r.intensityLevel
    """)
    List<Object[]> getCompletionRateByIntensity(@Param("userId") Long userId);

    // 특정 기간 동안의 일일 운동 시간 추이
    @Query("""
        SELECT r.routineDate as date,
        SUM(r.durationMinutes) as totalDuration,
        SUM(r.caloriesBurned) as totalCalories
        FROM ExerciseRoutine r
        WHERE r.user.id = :userId
        AND r.routineDate BETWEEN :startDate AND :endDate
        AND r.completionStatus = true
        GROUP BY r.routineDate
        ORDER BY r.routineDate
    """)
    List<Object[]> getDailyExerciseStats(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 가장 자주 수행한 운동 조회
    @Query("""
        SELECT r.exerciseBase.id as exerciseId,
        r.exerciseBase.name as exerciseName,
        COUNT(r) as count
        FROM ExerciseRoutine r
        WHERE r.user.id = :userId
        AND r.completionStatus = true
        GROUP BY r.exerciseBase.id, r.exerciseBase.name
        ORDER BY count DESC
        LIMIT 5
    """)
    List<Object[]> getMostPerformedExercises(@Param("userId") Long userId);

    // 페이징을 적용한 사용자 운동 루틴 조회
    Page<ExerciseRoutine> findByUserIdOrderByRoutineDateDesc(Long userId, Pageable pageable);

    // 필요한 경우 커스텀 삭제 메서드 추가
    @Modifying
    @Query("DELETE FROM ExerciseRoutine e WHERE e.id = :routineId")
    void deleteByRoutineId(@Param("routineId") Long routineId);

    @Query("SELECT er FROM ExerciseRoutine er " +
            "WHERE er.user.id = :userId " +
            "AND er.routineDate > :date")
    List<ExerciseRoutine> findByUserIdAndRoutineDateAfter(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );
}
