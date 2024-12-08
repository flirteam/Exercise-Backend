// ExerciseRecommendationService.java
package com.fitter.service.exercise;

import com.fitter.domain.exercise.*;
import com.fitter.domain.user.User;
import com.fitter.domain.user.UserPhysicalInfo;
import com.fitter.dto.response.ExerciseRecommendationResponse;
import com.fitter.exception.ExerciseNotFoundException;
import com.fitter.exception.UserNotFoundException;
import com.fitter.repository.exercise.*;
import com.fitter.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseRecommendationService {

    private final UserRepository userRepository;
    private final ExerciseBaseRepository exerciseBaseRepository;
    private final ExerciseRoutineRepository exerciseRoutineRepository;
    private final ExerciseRecommendationRepository recommendationRepository;
    private final ExercisePreferenceRepository preferenceRepository;
    private final ExerciseDifficultyMapRepository difficultyMapRepository;

    // 운동 추천 생성
    @Transactional
    public ExerciseRecommendationResponse createRecommendation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // 사용자 선호도 및 신체 정보 기반 운동 추천
        ExerciseRoutine recommendedRoutine = recommendExerciseRoutine(user);

        // 먼저 ExerciseRoutine을 저장
        ExerciseRoutine savedRoutine = exerciseRoutineRepository.save(recommendedRoutine);

        ExerciseRecommendation recommendation = ExerciseRecommendation.builder()
                .user(user)
                .exerciseRoutine(savedRoutine)  // 저장된 routine을 사용
                .recommendationDate(LocalDate.now())
                .caloriesBurned(savedRoutine.getCaloriesBurned())
                .completionStatus(false)
                .build();

        ExerciseRecommendation savedRecommendation = recommendationRepository.save(recommendation);
        return ExerciseRecommendationResponse.from(savedRecommendation);
    }

    // 사용자별 추천 이력 조회
    public List<ExerciseRecommendationResponse> getUserRecommendations(Long userId, LocalDate startDate, LocalDate endDate) {
        return recommendationRepository.findByUserIdAndRecommendationDateBetween(userId, startDate, endDate)
                .stream()
                .map(ExerciseRecommendationResponse::from)
                .collect(Collectors.toList());
    }

    // 추천 운동 완료 처리
    @Transactional
    public ExerciseRecommendationResponse completeRecommendation(Long recommendationId, String feedback) {
        ExerciseRecommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new ExerciseNotFoundException("Recommendation not found with id: " + recommendationId));

        recommendation.completeRecommendation(feedback);
        recommendation.getExerciseRoutine().completeExercise();

        return ExerciseRecommendationResponse.from(recommendation);
    }

    // 추천 운동 조회 메소드 추가
    public ExerciseRecommendation getRecommendation(Long recommendationId) {
        return recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new ExerciseNotFoundException("추천 운동을 찾을 수 없습니다."));
    }

    // 운동 루틴 추천 로직
    private ExerciseRoutine recommendExerciseRoutine(User user) {
        UserPhysicalInfo physicalInfo = user.getPhysicalInfo();
        ExercisePreference preference = preferenceRepository.findByUserId(user.getId()).orElse(null);

        // 적절한 운동 선택
        ExerciseBase exerciseBase = selectAppropriateExercise(physicalInfo, preference);

        // 운동 강도 결정
        ExerciseRoutine.IntensityLevel intensity = determineIntensityLevel(physicalInfo);

        // 운동 시간 계산
        int duration = calculateRecommendedDuration(physicalInfo);

        // 예상 칼로리 소모량 계산
        double caloriesBurned = calculateCaloriesBurned(physicalInfo, exerciseBase, duration, intensity);

        return ExerciseRoutine.builder()
                .user(user)
                .exerciseBase(exerciseBase)
                .durationMinutes(duration)
                .intensityLevel(intensity)
                .caloriesBurned(caloriesBurned)
                .routineDate(LocalDate.now())
                .build();
    }

    private ExerciseBase selectAppropriateExercise(UserPhysicalInfo physicalInfo, ExercisePreference preference) {
        List<ExerciseBase> candidateExercises = new ArrayList<>();

        if (preference != null) {
            // 1. 선호도 기반 운동 (40% 확률)
            if (Math.random() < 0.4) {
                List<ExerciseBase> preferredExercises = exerciseBaseRepository.findByPreference(
                        preference.getPreferredBodyPart(),
                        preference.getPreferredCategory()
                );
                if (!preferredExercises.isEmpty()) {
                    candidateExercises.addAll(preferredExercises);
                }
            }

            // 2. 균형잡힌 운동을 위한 다른 부위 운동 추가 (60% 확률)
            if (candidateExercises.isEmpty() || Math.random() >= 0.4) {
                LocalDate weekAgo = LocalDate.now().minusWeeks(1);
                List<ExerciseBase> balancedExercises = exerciseBaseRepository.findBalancedExercises(
                        preference.getPreferredBodyPart(),
                        physicalInfo.getUser().getId(),
                        weekAgo
                );
                if (!balancedExercises.isEmpty()) {
                    candidateExercises.addAll(balancedExercises);
                }
            }
        }

        // 3. BMI 기반 보완 검색
        if (candidateExercises.isEmpty()) {
            List<ExerciseBase> bmiBasedExercises = exerciseBaseRepository.findByBmiRange(
                    physicalInfo.getBmi() - 2,
                    physicalInfo.getBmi() + 2
            );
            candidateExercises.addAll(bmiBasedExercises);
        }

        // 4. 최근 운동과의 균형을 고려한 필터링
        return candidateExercises.stream()
                .filter(exercise -> !wasRecentlyRecommended(physicalInfo.getUser().getId(), exercise.getId()))
                .filter(exercise -> isBalancedWithRecentExercises(exercise, physicalInfo.getUser().getId()))
                .findFirst()
                .orElse(candidateExercises.isEmpty() ? null : candidateExercises.get(0));
    }

    // 최근 운동과의 균형을 체크하는 메서드
    private boolean isBalancedWithRecentExercises(ExerciseBase exercise, Long userId) {
        LocalDate weekAgo = LocalDate.now().minusWeeks(1);

        // 최근 일주일간의 운동 루틴 조회
        List<ExerciseRoutine> recentRoutines = exerciseRoutineRepository
                .findByUserIdAndRoutineDateAfter(userId, weekAgo);

        // 같은 부위 운동이 2회 이상 있으면 false
        long sameBodyPartCount = recentRoutines.stream()
                .map(ExerciseRoutine::getExerciseBase)
                .filter(e -> e.getBodyPart() == exercise.getBodyPart())
                .count();

        return sameBodyPartCount < 2;
    }

    private boolean wasRecentlyRecommended(Long userId, Long exerciseId) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        return recommendationRepository.existsByUserIdAndExerciseRoutine_ExerciseBase_IdAndCreatedAtAfter(
                userId, exerciseId, oneWeekAgo);
    }

    private ExerciseRoutine.IntensityLevel determineIntensityLevel(UserPhysicalInfo physicalInfo) {
        return difficultyMapRepository.findAll().stream()
                .filter(map -> map.isApplicable(
                        physicalInfo.getBmi(),
                        physicalInfo.getAge(),
                        physicalInfo.getActivityLevel()))
                .findFirst()
                .map(ExerciseDifficultyMap::getRecommendedIntensity)
                .orElse(ExerciseRoutine.IntensityLevel.중간);
    }

    private int calculateRecommendedDuration(UserPhysicalInfo physicalInfo) {
        double weightDiff = Math.abs(physicalInfo.getCurrentWeight() - physicalInfo.getTargetWeight());
        int baseDuration = 30;
        int additionalMinutes = (int) (weightDiff * 2);
        return Math.min(baseDuration + additionalMinutes, 90);
    }

    private double calculateCaloriesBurned(UserPhysicalInfo physicalInfo, ExerciseBase exercise,
                                           int duration, ExerciseRoutine.IntensityLevel intensity) {
        return exercise.calculateActualCaloriesBurned(physicalInfo.getCurrentWeight(), duration) *
                intensity.getFactor();
    }
}