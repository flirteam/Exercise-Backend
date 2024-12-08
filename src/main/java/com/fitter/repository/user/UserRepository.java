// UserRepository.java
package com.fitter.repository.user;

import com.fitter.domain.user.User;
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
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);

    // 특정 활동 레벨을 가진 사용자 목록 조회
    @Query("SELECT u FROM User u JOIN u.physicalInfo p WHERE p.activityLevel = :activityLevel")
    List<User> findByActivityLevel(@Param("activityLevel") Integer activityLevel);

    // BMI 범위에 해당하는 사용자 목록 조회
    @Query("SELECT u FROM User u JOIN u.physicalInfo p WHERE p.bmi BETWEEN :minBmi AND :maxBmi")
    List<User> findByBmiRange(@Param("minBmi") Double minBmi, @Param("maxBmi") Double maxBmi);

    // 목표 체중에 도달한 사용자 목록 조회
    @Query("SELECT u FROM User u JOIN u.physicalInfo p WHERE ABS(p.currentWeight - p.targetWeight) <= :threshold")
    List<User> findUsersReachedTargetWeight(@Param("threshold") Double threshold);

    // 특정 기간 내 가입한 사용자 수 조회
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate")
    Long countUsersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 사용자의 운동 루틴 개수 조회
    @Query("SELECT COUNT(er) FROM User u JOIN u.exerciseRoutines er WHERE u.id = :userId")
    Long countExerciseRoutines(@Param("userId") Long userId);

    // 최근 로그인한 순서로 사용자 목록 조회 (페이징)
    @Query("SELECT u FROM User u ORDER BY u.updatedAt DESC")
    Page<User> findAllOrderByLastLoginDesc(Pageable pageable);
}