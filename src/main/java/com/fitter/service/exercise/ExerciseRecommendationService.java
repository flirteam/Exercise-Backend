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
import java.util.Collections;
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
    public List<ExerciseRecommendationResponse> createRecommendation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // 4개의 운동 추천
        List<ExerciseBase> selectedExercises = selectMultipleExercises(user.getPhysicalInfo(), 4);

        List<ExerciseRecommendationResponse> recommendations = new ArrayList<>();

        for (ExerciseBase exerciseBase : selectedExercises) {
            ExerciseRoutine.IntensityLevel intensity = determineIntensityLevel(user.getPhysicalInfo());
            int duration = calculateRecommendedDuration(user.getPhysicalInfo());
            double caloriesBurned = calculateCaloriesBurned(user.getPhysicalInfo(), exerciseBase, duration, intensity);

            ExerciseRoutine routine = ExerciseRoutine.builder()
                    .user(user)
                    .exerciseBase(exerciseBase)
                    .durationMinutes(duration)
                    .intensityLevel(intensity)
                    .caloriesBurned(caloriesBurned)
                    .routineDate(LocalDate.now())
                    .build();

            ExerciseRoutine savedRoutine = exerciseRoutineRepository.save(routine);

            ExerciseRecommendation recommendation = ExerciseRecommendation.builder()
                    .user(user)
                    .exerciseRoutine(savedRoutine)
                    .recommendationDate(LocalDate.now())
                    .caloriesBurned(savedRoutine.getCaloriesBurned())
                    .completionStatus(false)
                    .build();

            recommendations.add(ExerciseRecommendationResponse.from(
                    recommendationRepository.save(recommendation)));
        }

        return recommendations;
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

    /*// 운동 루틴 추천 로직
    private ExerciseRoutine recommendExerciseRoutine(User user) {
        UserPhysicalInfo physicalInfo = user.getPhysicalInfo();
        ExercisePreference preference = preferenceRepository.findByUserId(user.getId()).orElse(null);

        // 적절한 운동 선택
        ExerciseBase exerciseBase = selectMultipleExercises(physicalInfo);

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
    }*/

    private List<ExerciseBase> selectMultipleExercises(UserPhysicalInfo physicalInfo, int count) {
        List<ExerciseBase> candidateExercises = new ArrayList<>();

        // 1. 선호도 확인
        ExercisePreference preference = preferenceRepository.findByUserId(physicalInfo.getUser().getId())
                .orElse(null);

        // 선호도가 설정된 경우
        if (preference != null) {
            // 선호하는 신체 부위의 운동들을 가져옴
            List<ExerciseBase> preferredBodyPartExercises = exerciseBaseRepository.findByBodyPart(
                    preference.getPreferredBodyPart()
            );

            // 선호 부위 운동들 중에서 BMI 기반으로 필터링
            if (physicalInfo.getBmi() < 18.5) {  // 저체중
                candidateExercises.addAll(
                        preferredBodyPartExercises.stream()
                                .filter(e -> e.getExerciseCategory() == ExerciseBase.ExerciseCategory.상체근력운동
                                        || e.getExerciseCategory() == ExerciseBase.ExerciseCategory.하체근력운동
                                        || e.getExerciseCategory() == ExerciseBase.ExerciseCategory.코어강화운동)
                                .collect(Collectors.toList())
                );
            }
            else if (physicalInfo.getBmi() >= 18.5 && physicalInfo.getBmi() < 23.0) {  // 정상체중
                candidateExercises.addAll(
                        preferredBodyPartExercises.stream()
                                .filter(e -> e.getExerciseCategory() == ExerciseBase.ExerciseCategory.전신강화운동
                                        || e.getExerciseCategory() == ExerciseBase.ExerciseCategory.코어강화운동
                                        || e.getExerciseCategory() == ExerciseBase.ExerciseCategory.유산소운동
                                        || e.getExerciseCategory() == ExerciseBase.ExerciseCategory.유연성및스트레칭운동
                                        || e.getExerciseCategory() == ExerciseBase.ExerciseCategory.상체근력운동
                                        || e.getExerciseCategory() == ExerciseBase.ExerciseCategory.하체근력운동)
                                .collect(Collectors.toList())
                );
            }
            else if (physicalInfo.getBmi() >= 23.0 && physicalInfo.getBmi() < 25.0) {  // 과체중
                candidateExercises.addAll(
                        preferredBodyPartExercises.stream()
                                .filter(e -> e.getExerciseCategory() == ExerciseBase.ExerciseCategory.유산소운동
                                        || e.getExerciseCategory() == ExerciseBase.ExerciseCategory.전신강화운동
                                        || e.getExerciseCategory() == ExerciseBase.ExerciseCategory.유연성및스트레칭운동)
                                .collect(Collectors.toList())
                );
            }
            else {  // 비만 (BMI >= 25.0)
                candidateExercises.addAll(
                        preferredBodyPartExercises.stream()
                                .filter(e -> e.getExerciseCategory() == ExerciseBase.ExerciseCategory.유산소운동
                                        || e.getExerciseCategory() == ExerciseBase.ExerciseCategory.유연성및스트레칭운동)
                                .collect(Collectors.toList())
                );
            }
            // 필터링 결과가 없으면 선호 부위 운동 전체를 후보로 사용
            if (candidateExercises.isEmpty()) {
                candidateExercises.addAll(preferredBodyPartExercises);
            }
        }
        // 선호도가 설정되지 않은 경우 BMI 기반 추천
        else {
            if (physicalInfo.getBmi() < 18.5) {  // 저체중
                List<ExerciseBase> exercises = new ArrayList<>();
                exercises.addAll(exerciseBaseRepository.findByExerciseCategory(
                        ExerciseBase.ExerciseCategory.상체근력운동));
                exercises.addAll(exerciseBaseRepository.findByExerciseCategory(
                        ExerciseBase.ExerciseCategory.하체근력운동));
                exercises.addAll(exerciseBaseRepository.findByExerciseCategory(
                        ExerciseBase.ExerciseCategory.코어강화운동));
                candidateExercises.addAll(exercises);
            }
            else if (physicalInfo.getBmi() >= 18.5 && physicalInfo.getBmi() < 23.0) {  // 정상체중
                List<ExerciseBase> exercises = new ArrayList<>();
                exercises.addAll(exerciseBaseRepository.findByExerciseCategory(
                        ExerciseBase.ExerciseCategory.전신강화운동));
                exercises.addAll(exerciseBaseRepository.findByExerciseCategory(
                        ExerciseBase.ExerciseCategory.코어강화운동));
                exercises.addAll(exerciseBaseRepository.findByExerciseCategory(
                        ExerciseBase.ExerciseCategory.유산소운동));
                exercises.addAll(exerciseBaseRepository.findByExerciseCategory(
                        ExerciseBase.ExerciseCategory.상체근력운동));
                exercises.addAll(exerciseBaseRepository.findByExerciseCategory(
                        ExerciseBase.ExerciseCategory.하체근력운동));
                exercises.addAll(exerciseBaseRepository.findByExerciseCategory(
                        ExerciseBase.ExerciseCategory.유연성및스트레칭운동));
                candidateExercises.addAll(exercises);
            }
            else if (physicalInfo.getBmi() >= 23.0 && physicalInfo.getBmi() < 25.0) {  // 과체중
                List<ExerciseBase> exercises = new ArrayList<>();
                exercises.addAll(exerciseBaseRepository.findByExerciseCategory(
                        ExerciseBase.ExerciseCategory.유산소운동));
                exercises.addAll(exerciseBaseRepository.findByExerciseCategory(
                        ExerciseBase.ExerciseCategory.전신강화운동));
                exercises.addAll(exerciseBaseRepository.findByExerciseCategory(
                        ExerciseBase.ExerciseCategory.유연성및스트레칭운동));
                candidateExercises.addAll(exercises);
            }
            else {  // 비만 (BMI >= 25.0)
                List<ExerciseBase> exercises = new ArrayList<>();
                exercises.addAll(exerciseBaseRepository.findByExerciseCategory(
                        ExerciseBase.ExerciseCategory.유산소운동));
                exercises.addAll(exerciseBaseRepository.findByExerciseCategory(
                        ExerciseBase.ExerciseCategory.유연성및스트레칭운동));
                candidateExercises.addAll(exercises);
            }
        }

        // 후보 운동들을 섞음
        Collections.shuffle(candidateExercises);

        // 최근 추천되지 않은 운동들 필터링
        List<ExerciseBase> filteredExercises = candidateExercises.stream()
                .filter(exercise -> !wasRecentlyRecommended(physicalInfo.getUser().getId(), exercise.getId()))
                .limit(count)
                .collect(Collectors.toList());

        // 필터링된 운동이 충분하지 않으면 다시 전체 후보에서 선택
        if (filteredExercises.size() < count) {
            Collections.shuffle(candidateExercises);
            filteredExercises = candidateExercises.stream()
                    .limit(count)
                    .collect(Collectors.toList());
        }

        return filteredExercises;
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
        int baseDuration = 15;
        int additionalMinutes = (int) (weightDiff * 1.28);
        return Math.min(baseDuration + additionalMinutes, 30);
    }

    private double calculateCaloriesBurned(UserPhysicalInfo physicalInfo, ExerciseBase exercise,
                                           int duration, ExerciseRoutine.IntensityLevel intensity) {
        return exercise.calculateActualCaloriesBurned(physicalInfo.getCurrentWeight(), duration) *
                intensity.getFactor();
    }
}