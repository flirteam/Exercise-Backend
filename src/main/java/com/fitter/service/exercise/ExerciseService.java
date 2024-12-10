// ExerciseService.java
package com.fitter.service.exercise;

import com.fitter.domain.exercise.*;
import com.fitter.domain.user.User;
import com.fitter.domain.user.UserPhysicalInfo;
import com.fitter.dto.request.ExerciseRoutineRequest;
import com.fitter.dto.response.ExerciseDetailResponse;
import com.fitter.dto.response.ExerciseRoutineResponse;
import com.fitter.exception.ExerciseNotFoundException;
import com.fitter.exception.UserNotFoundException;
import com.fitter.repository.exercise.*;
import com.fitter.repository.user.UserRepository;
import com.fitter.service.exercise.MetabolicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ExerciseService {

    private final UserRepository userRepository;
    private final ExerciseBaseRepository exerciseBaseRepository;
    private final ExerciseRoutineRepository exerciseRoutineRepository;
    private final ExerciseDifficultyMapRepository difficultyMapRepository;
    private final ExercisePreferenceRepository preferenceRepository;
    private final MetabolicService metabolicService;

    // 운동 루틴 생성
    @Transactional
    public ExerciseRoutineResponse createExerciseRoutine(Long userId, ExerciseRoutineRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        ExerciseBase exerciseBase = exerciseBaseRepository.findById(request.getExerciseBaseId())
                .orElseThrow(() -> new ExerciseNotFoundException("Exercise not found with id: " + request.getExerciseBaseId()));

        ExerciseRoutine routine = ExerciseRoutine.builder()
                .user(user)
                .exerciseBase(exerciseBase)
                .durationMinutes(request.getDurationMinutes())
                .intensityLevel(request.getIntensityLevel())
                .routineDate(request.getRoutineDate())
                .completionStatus(false)
                .build();

        // 칼로리 소모량 계산
        double caloriesBurned = calculateCaloriesBurned(user.getPhysicalInfo(), exerciseBase,
                request.getDurationMinutes(), request.getIntensityLevel());
        routine.updateCaloriesBurned(caloriesBurned);

        return ExerciseRoutineResponse.from(exerciseRoutineRepository.save(routine));
    }

    // 사용자별 운동 루틴 조회
    public List<ExerciseRoutineResponse> getUserExerciseRoutines(Long userId, LocalDate startDate, LocalDate endDate) {
        List<ExerciseRoutine> routines = exerciseRoutineRepository
                .findByUserIdAndRoutineDateBetween(userId, startDate, endDate);

        return routines.stream()
                .map(ExerciseRoutineResponse::from)
                .collect(Collectors.toList());
    }

    // 운동 추천
    public List<ExerciseRoutineResponse> recommendExercises(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        UserPhysicalInfo physicalInfo = user.getPhysicalInfo();
        ExercisePreference preference = preferenceRepository.findByUserId(userId).orElse(null);

        // 사용자 BMI와 활동량에 따른 적절한 운동 강도 결정
        ExerciseRoutine.IntensityLevel recommendedIntensity = determineRecommendedIntensity(physicalInfo);

        // 사용자 선호도와 신체 조건을 고려한 운동 목록 조회
        List<ExerciseBase> recommendedExercises = findRecommendedExercises(physicalInfo, preference);

        // 운동 루틴 생성
        return recommendedExercises.stream()
                .map(exercise -> createRecommendedRoutine(user, exercise, recommendedIntensity))
                .map(ExerciseRoutineResponse::from)
                .collect(Collectors.toList());
    }

    // 칼로리 소모량 계산
    private double calculateCaloriesBurned(UserPhysicalInfo physicalInfo, ExerciseBase exercise,
                                           int durationMinutes, ExerciseRoutine.IntensityLevel intensity) {
        double baseCalories = exercise.calculateActualCaloriesBurned(physicalInfo.getCurrentWeight(), durationMinutes);
        return baseCalories * intensity.getFactor();
    }

    // 권장 운동 강도 결정
    private ExerciseRoutine.IntensityLevel determineRecommendedIntensity(UserPhysicalInfo physicalInfo) {
        return difficultyMapRepository.findAll().stream()
                .filter(map -> map.isApplicable(
                        physicalInfo.getBmi(),
                        physicalInfo.getAge(),
                        physicalInfo.getActivityLevel()))
                .findFirst()
                .map(ExerciseDifficultyMap::getRecommendedIntensity)
                .orElse(ExerciseRoutine.IntensityLevel.중간);
    }

    // 추천 운동 목록 조회
    private List<ExerciseBase> findRecommendedExercises(UserPhysicalInfo physicalInfo,
                                                        ExercisePreference preference) {
        if (preference != null) {
            return exerciseBaseRepository.findByPreference(
                    preference.getPreferredBodyPart(),
                    preference.getPreferredCategory()
            );
        }
        return exerciseBaseRepository.findByBmiRange(
                physicalInfo.getBmi() - 2,
                physicalInfo.getBmi() + 2
        );
    }

    // 추천 운동 루틴 생성
    private ExerciseRoutine createRecommendedRoutine(User user, ExerciseBase exercise,
                                                     ExerciseRoutine.IntensityLevel intensity) {
        int recommendedDuration = calculateRecommendedDuration(user.getPhysicalInfo());
        double caloriesBurned = calculateCaloriesBurned(
                user.getPhysicalInfo(), exercise, recommendedDuration, intensity);

        return ExerciseRoutine.builder()
                .user(user)
                .exerciseBase(exercise)
                .durationMinutes(recommendedDuration)
                .intensityLevel(intensity)
                .caloriesBurned(caloriesBurned)
                .routineDate(LocalDate.now())
                .build();
    }

    // 권장 운동 시간 계산
    private int calculateRecommendedDuration(UserPhysicalInfo physicalInfo) {
        double currentWeight = physicalInfo.getCurrentWeight();
        double targetWeight = physicalInfo.getTargetWeight();
        double weightDiff = Math.abs(currentWeight - targetWeight);

        int baseDuration = 15; // 기본 30분
        int additionalMinutes = (int) (weightDiff * 1.25); // 체중 차이 1kg당 2분 추가

        return Math.min(baseDuration + additionalMinutes, 30); // 최대 90분으로 제한
    }

    // 운동 카테고리별 조회
    public List<ExerciseDetailResponse> getExercisesByCategory(ExerciseBase.ExerciseCategory category) {
        log.debug("Fetching exercises for category: {}", category);
        List<ExerciseBase> exercises = exerciseBaseRepository.findByCategory(category);
        return exercises.stream()
                .map(ExerciseDetailResponse::from)
                .collect(Collectors.toList());
    }

    // 운동 이름으로 검색
    public List<ExerciseDetailResponse> searchExercises(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }
        log.debug("Searching exercises with keyword: {}", keyword);
        return exerciseBaseRepository.searchByName(keyword).stream()
                .map(ExerciseDetailResponse::from)
                .collect(Collectors.toList());
    }

    // 운동 상세 정보 조회
    public ExerciseDetailResponse getExerciseDetail(Long id) {
        log.debug("Fetching exercise detail for id: {}", id);
        ExerciseBase exercise = exerciseBaseRepository.findById(id)
                .orElseThrow(() -> new ExerciseNotFoundException("해당 운동을 찾을 수 없습니다. ID: " + id));
        return ExerciseDetailResponse.from(exercise);
    }

    // 신체 부위별 운동 조회
    public List<ExerciseDetailResponse> getExercisesByBodyPart(ExerciseBase.BodyPart bodyPart) {
        log.debug("Fetching exercises for body part: {}", bodyPart);
        return exerciseBaseRepository.searchByBodyPart(bodyPart).stream()
                .map(ExerciseDetailResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional  // 트랜잭션 어노테이션 필수
    public ExerciseRoutineResponse updateExerciseRoutine(Long routineId, ExerciseRoutineRequest request) {
        ExerciseRoutine routine = exerciseRoutineRepository.findById(routineId)
                .orElseThrow(() -> new ExerciseNotFoundException("운동 루틴을 찾을 수 없습니다: " + routineId));

        // 명시적 업데이트 수행
        if (request.getDurationMinutes() != null) {
            routine.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getIntensityLevel() != null) {
            routine.setIntensityLevel(request.getIntensityLevel());
        }
        if (request.getRoutineDate() != null) {
            routine.setRoutineDate(request.getRoutineDate());
        }

        // 칼로리 재계산
        double calories = routine.getExerciseBase().calculateActualCaloriesBurned(
                routine.getUser().getPhysicalInfo().getCurrentWeight(),
                routine.getDurationMinutes()
        ) * routine.getIntensityLevel().getFactor();
        routine.setCaloriesBurned(calories);

        // 변경사항 저장
        ExerciseRoutine updatedRoutine = exerciseRoutineRepository.save(routine);

        // 응답 생성 및 반환
        return ExerciseRoutineResponse.from(updatedRoutine);
    }

    @Transactional
    public void deleteExerciseRoutine(Long routineId) {
        ExerciseRoutine routine = exerciseRoutineRepository.findById(routineId)
                .orElseThrow(() -> new ExerciseNotFoundException("Exercise routine not found with id: " + routineId));

        exerciseRoutineRepository.delete(routine);
        // 명시적으로 flush 호출하여 즉시 데이터베이스에 반영
        exerciseRoutineRepository.flush();
    }


}