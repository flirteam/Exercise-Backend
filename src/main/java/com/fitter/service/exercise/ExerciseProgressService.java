//ExerciseProgressService
package com.fitter.service.exercise;

import com.fitter.domain.exercise.*;
import com.fitter.dto.response.ExerciseProgressResponse;
import com.fitter.exception.ExerciseNotFoundException;
import com.fitter.exception.GoalNotFoundException;
import com.fitter.repository.exercise.ExerciseRoutineRepository;
import com.fitter.repository.exercise.UserExerciseProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseProgressService {

    private final ExerciseRoutineRepository routineRepository;
    private final UserExerciseProgressRepository progressRepository;
    private final ExerciseGoalService exerciseGoalService;  // 주입

    @Transactional
    public ExerciseProgressResponse recordProgress(
            Long userId,
            Long routineId,
            int actualDuration,
            Integer difficultyRating,
            String feedback) {

        // 1. 운동 루틴 조회 및 검증
        ExerciseRoutine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new ExerciseNotFoundException("Exercise routine not found with id: " + routineId));

        if (!routine.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User ID does not match the routine's owner.");
        }

        // 2. 진행상황 기록
        UserExerciseProgress progress = createAndSaveProgress(routine, actualDuration, difficultyRating, feedback);

        // 3. 운동 완료 처리
        routine.completeExercise();

        // 4. 목표 달성률 업데이트
        exerciseGoalService.updateGoalAchievement(userId);

        return ExerciseProgressResponse.from(progress);
    }

    private UserExerciseProgress createAndSaveProgress(
            ExerciseRoutine routine,
            int actualDuration,
            Integer difficultyRating,
            String feedback) {

        UserExerciseProgress progress = UserExerciseProgress.builder()
                .user(routine.getUser())
                .exerciseRoutine(routine)
                .completedDate(LocalDate.now())
                .actualDurationMinutes(actualDuration)
                .build();

        if (difficultyRating != null) {
            progress.addFeedback(difficultyRating, feedback);
        }

        routine.completeExercise();
        progress.updateProgress(actualDuration);

        return progressRepository.save(progress);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void updateGoalAchievement(Long userId) {
        exerciseGoalService.updateGoalAchievement(userId);
    }

    // 사용자별 운동 진행 상황 조회
    public List<ExerciseProgressResponse> getUserProgress(Long userId, LocalDate startDate, LocalDate endDate) {
        return progressRepository.findByUserIdAndCompletedDateBetween(userId, startDate, endDate)
                .stream()
                .map(ExerciseProgressResponse::from)
                .collect(Collectors.toList());
    }

    // 목표 달성률 계산
    public double calculateGoalAchievementRate(Long userId, LocalDate date) {
        List<UserExerciseProgress> dayProgress = progressRepository
                .findByUserIdAndCompletedDate(userId, date);

        if (dayProgress.isEmpty()) {
            return 0.0;
        }

        double totalActualCalories = dayProgress.stream()
                .mapToDouble(UserExerciseProgress::getActualCaloriesBurned)
                .sum();

        double totalTargetCalories = dayProgress.stream()
                .map(UserExerciseProgress::getExerciseRoutine)
                .mapToDouble(ExerciseRoutine::getCaloriesBurned)
                .sum();

        return (totalActualCalories / totalTargetCalories) * 100;
    }

    // 운동 효율성 분석
    public double analyzeExerciseEfficiency(Long userId, LocalDate startDate, LocalDate endDate) {
        List<UserExerciseProgress> progressList = progressRepository
                .findByUserIdAndCompletedDateBetween(userId, startDate, endDate);

        if (progressList.isEmpty()) {
            return 0.0;
        }

        return progressList.stream()
                .mapToInt(UserExerciseProgress::calculateEfficiencyScore)
                .average()
                .orElse(0.0);
    }

    // 난이도 적절성 분석
    public String analyzeDifficultyAppropriateness(Long userId) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<UserExerciseProgress> recentProgress = progressRepository
                .findByUserIdAndCreatedAtAfter(userId, oneMonthAgo);

        if (recentProgress.isEmpty()) {
            return "아직 충분한 운동 데이터가 없습니다.";
        }

        double averageDifficulty = recentProgress.stream()
                .filter(p -> p.getDifficultyRating() != null)
                .mapToInt(UserExerciseProgress::getDifficultyRating)
                .average()
                .orElse(0);

        double achievementRate = recentProgress.stream()
                .mapToDouble(UserExerciseProgress::calculateAchievementRate)
                .average()
                .orElse(0);

        return generateDifficultyAnalysis(averageDifficulty, achievementRate);
    }

    private String generateDifficultyAnalysis(double averageDifficulty, double achievementRate) {
        StringBuilder analysis = new StringBuilder("운동 난이도 분석: ");

        if (averageDifficulty > 4 && achievementRate < 80) {
            analysis.append("현재 운동 난이도가 다소 높습니다. 강도를 낮추는 것을 고려해보세요.");
        } else if (averageDifficulty < 2 && achievementRate > 110) {
            analysis.append("현재 운동 난이도가 낮습니다. 강도를 높여 더 효과적인 운동을 할 수 있습니다.");
        } else if (achievementRate >= 90 && achievementRate <= 110) {
            analysis.append("현재 운동 난이도가 적절합니다. 현재 수준을 유지하세요.");
        } else {
            analysis.append("운동 강도와 목표를 재조정할 필요가 있습니다.");
        }

        return analysis.toString();
    }
}