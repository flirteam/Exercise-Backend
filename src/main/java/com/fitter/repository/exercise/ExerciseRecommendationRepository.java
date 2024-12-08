// ExerciseRecommendationRepository.java
package com.fitter.repository.exercise;

import com.fitter.domain.exercise.ExerciseRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface ExerciseRecommendationRepository extends JpaRepository<ExerciseRecommendation, Long> {

    // 사용자별 추천 이력 조회
    List<ExerciseRecommendation> findByUserIdOrderByRecommendationDateDesc(Long userId);

    // 기간별 추천 이력 조회
    List<ExerciseRecommendation> findByUserIdAndRecommendationDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    // 최근 추천 여부 확인
    boolean existsByUserIdAndExerciseRoutine_ExerciseBase_IdAndCreatedAtAfter(
            Long userId,
            Long exerciseBaseId,
            LocalDateTime dateTime
    );

    // 완료된 추천 운동 조회
    @Query("""
        SELECT er FROM ExerciseRecommendation er
        WHERE er.user.id = :userId
        AND er.completionStatus = true
        AND er.recommendationDate BETWEEN :startDate AND :endDate
    """)
    List<ExerciseRecommendation> findCompletedRecommendations(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 추천 운동 완료율 통계
    @Query("""
        SELECT new map(
            COUNT(er) as totalRecommendations,
            SUM(CASE WHEN er.completionStatus = true THEN 1 ELSE 0 END) as completedRecommendations,
            AVG(CASE WHEN er.completionStatus = true THEN er.caloriesBurned ELSE 0 END) as avgCaloriesBurned
        )
        FROM ExerciseRecommendation er
        WHERE er.user.id = :userId
        AND er.recommendationDate BETWEEN :startDate AND :endDate
    """)
    Map<String, Object> getRecommendationStatistics(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}