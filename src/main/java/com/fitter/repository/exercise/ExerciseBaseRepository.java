// ExerciseBaseRepository.java
package com.fitter.repository.exercise;

import com.fitter.domain.exercise.ExerciseBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExerciseBaseRepository extends JpaRepository<ExerciseBase, Long> {

    // 신체 부위별 운동 조회
    List<ExerciseBase> findByBodyPart(ExerciseBase.BodyPart bodyPart);

    // 운동 카테고리별 조회
    List<ExerciseBase> findByExerciseCategory(ExerciseBase.ExerciseCategory category);

    // 칼로리 소모량 범위로 조회
    @Query("SELECT e FROM ExerciseBase e WHERE e.baseCaloriesBurned BETWEEN :minCalories AND :maxCalories")
    List<ExerciseBase> findByCalorieRange(
            @Param("minCalories") Double minCalories,
            @Param("maxCalories") Double maxCalories
    );

    // BMI 범위에 적합한 운동 조회
    @Query("""
        SELECT e FROM ExerciseBase e 
        WHERE (:minBmi IS NULL OR e.baseCaloriesBurned >= :minBmi * 0.1) 
        AND (:maxBmi IS NULL OR e.baseCaloriesBurned <= :maxBmi * 0.15)
        ORDER BY e.baseCaloriesBurned
    """)
    List<ExerciseBase> findByBmiRange(
            @Param("minBmi") Double minBmi,
            @Param("maxBmi") Double maxBmi
    );

    // 선호도 기반 운동 조회
    @Query("""
        SELECT e FROM ExerciseBase e 
        WHERE (:bodyPart IS NULL OR e.bodyPart = :bodyPart)
        AND (:category IS NULL OR e.exerciseCategory = :category)
        ORDER BY e.baseCaloriesBurned DESC
    """)
    List<ExerciseBase> findByPreference(
            @Param("bodyPart") ExerciseBase.BodyPart bodyPart,
            @Param("category") ExerciseBase.ExerciseCategory category
    );

    // 키워드로 운동 검색
    @Query("""
        SELECT e FROM ExerciseBase e 
        WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<ExerciseBase> searchByKeyword(@Param("keyword") String keyword);

    // 특정 운동과 유사한 운동 추천
    @Query("""
        SELECT e FROM ExerciseBase e 
        WHERE (e.bodyPart = :bodyPart OR e.exerciseCategory = :category)
        AND e.id != :exerciseId 
        ORDER BY FUNCTION('ABS', e.baseCaloriesBurned - :calories)
        LIMIT 5
    """)
    List<ExerciseBase> findSimilarExercises(
            @Param("exerciseId") Long exerciseId,
            @Param("bodyPart") ExerciseBase.BodyPart bodyPart,
            @Param("category") ExerciseBase.ExerciseCategory category,
            @Param("calories") Double calories
    );

    // 카테고리별 운동 조회
    @Query("SELECT e FROM ExerciseBase e WHERE e.exerciseCategory = :category")
    List<ExerciseBase> findByCategory(@Param("category") ExerciseBase.ExerciseCategory category);

    // 운동 이름으로 검색
    @Query("SELECT e FROM ExerciseBase e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ExerciseBase> searchByName(@Param("keyword") String keyword);

    // 신체 부위별 운동 조회 (메서드 이름 변경)
    @Query("SELECT e FROM ExerciseBase e WHERE e.bodyPart = :bodyPart")
    List<ExerciseBase> searchByBodyPart(@Param("bodyPart") ExerciseBase.BodyPart bodyPart);

    // 카테고리와 신체 부위로 운동 조회
    @Query("SELECT e FROM ExerciseBase e WHERE e.exerciseCategory = :category " +
            "AND e.bodyPart = :bodyPart")
    List<ExerciseBase> findByCategoryAndBodyPart(
            @Param("category") ExerciseBase.ExerciseCategory category,
            @Param("bodyPart") ExerciseBase.BodyPart bodyPart
    );

    // 균형잡힌 운동 추천을 위한 새 메서드
    @Query("""
    SELECT e FROM ExerciseBase e 
    WHERE e.bodyPart != :excludeBodyPart 
    AND e.id NOT IN (
        SELECT er.exerciseBase.id 
        FROM ExerciseRoutine er 
        WHERE er.user.id = :userId 
        AND er.routineDate > :recentDate
    )
    ORDER BY FUNCTION('RAND')
""")
    List<ExerciseBase> findBalancedExercises(
            @Param("excludeBodyPart") ExerciseBase.BodyPart excludeBodyPart,
            @Param("userId") Long userId,
            @Param("recentDate") LocalDate recentDate
    );
}