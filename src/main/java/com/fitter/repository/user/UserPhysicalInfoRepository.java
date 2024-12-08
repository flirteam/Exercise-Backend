//UserPhysicalInfoRepository.java
package com.fitter.repository.user;

import com.fitter.domain.user.UserPhysicalInfo;
import com.fitter.domain.user.UserPhysicalInfo.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPhysicalInfoRepository extends JpaRepository<UserPhysicalInfo, Long> {

    // 사용자 ID로 신체 정보 조회
    Optional<UserPhysicalInfo> findByUserId(Long userId);

    // BMI 범위에 해당하는 사용자들의 신체 정보 조회
    @Query("SELECT p FROM UserPhysicalInfo p WHERE p.bmi BETWEEN :minBmi AND :maxBmi")
    List<UserPhysicalInfo> findByBmiRange(
            @Param("minBmi") Double minBmi,
            @Param("maxBmi") Double maxBmi
    );

    // 특정 활동 레벨을 가진 사용자들의 신체 정보 조회
    List<UserPhysicalInfo> findByActivityLevel(Integer activityLevel);

    // 특정 나이대의 사용자들의 신체 정보 조회
    @Query("SELECT p FROM UserPhysicalInfo p WHERE p.age BETWEEN :minAge AND :maxAge")
    List<UserPhysicalInfo> findByAgeRange(
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge
    );

    // 성별에 따른 사용자들의 신체 정보 조회
    List<UserPhysicalInfo> findByGender(Gender gender);

    // 목표 체중 달성률에 따른 사용자들의 신체 정보 조회
    @Query("SELECT p FROM UserPhysicalInfo p WHERE ABS(p.currentWeight - p.targetWeight) / p.targetWeight * 100 <= :threshold")
    List<UserPhysicalInfo> findByWeightAchievementRate(@Param("threshold") Double threshold);

    // BMI 카테고리별 사용자 수 통계
    @Query("SELECT " +
            "CASE " +
            "  WHEN p.bmi < 18.5 THEN '저체중' " +
            "  WHEN p.bmi < 23.0 THEN '정상' " +
            "  WHEN p.bmi < 25.0 THEN '과체중' " +
            "  ELSE '비만' " +
            "END as bmiCategory, " +
            "COUNT(p) as count " +
            "FROM UserPhysicalInfo p " +
            "GROUP BY " +
            "CASE " +
            "  WHEN p.bmi < 18.5 THEN '저체중' " +
            "  WHEN p.bmi < 23.0 THEN '정상' " +
            "  WHEN p.bmi < 25.0 THEN '과체중' " +
            "  ELSE '비만' " +
            "END")
    List<Object[]> getBmiStatistics();

    // 특정 기간 동안 신체 정보가 업데이트된 사용자 조회
    @Query("SELECT p FROM UserPhysicalInfo p WHERE p.updatedAt BETWEEN :startDate AND :endDate")
    List<UserPhysicalInfo> findByUpdateDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 체중 변화가 있는 사용자들의 신체 정보 조회 (페이징)
    @Query("SELECT p FROM UserPhysicalInfo p WHERE p.currentWeight != p.targetWeight ORDER BY ABS(p.currentWeight - p.targetWeight) DESC")
    Page<UserPhysicalInfo> findByWeightChanges(Pageable pageable);

    // 특정 BMI 이상이면서 특정 활동 레벨을 가진 사용자들의 신체 정보 조회
    @Query("SELECT p FROM UserPhysicalInfo p WHERE p.bmi >= :minBmi AND p.activityLevel = :activityLevel")
    List<UserPhysicalInfo> findByBmiAndActivityLevel(
            @Param("minBmi") Double minBmi,
            @Param("activityLevel") Integer activityLevel
    );

    // 체지방률 범위에 해당하는 사용자들의 신체 정보 조회
    @Query("SELECT p FROM UserPhysicalInfo p WHERE p.bodyFatPercentage BETWEEN :minFat AND :maxFat")
    List<UserPhysicalInfo> findByBodyFatRange(
            @Param("minFat") Double minFat,
            @Param("maxFat") Double maxFat
    );

    // 활동 대사량(AMR)이 특정 값 이상인 사용자들의 신체 정보 조회
    List<UserPhysicalInfo> findByActiveMetabolicRateGreaterThanEqual(Double amr);

    // 기초 대사량(BMR)이 특정 범위에 있는 사용자들의 신체 정보 조회
    @Query("SELECT p FROM UserPhysicalInfo p WHERE p.basalMetabolicRate BETWEEN :minBmr AND :maxBmr")
    List<UserPhysicalInfo> findByBmrRange(
            @Param("minBmr") Double minBmr,
            @Param("maxBmr") Double maxBmr
    );
}