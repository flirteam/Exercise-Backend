//ExerciseGoalService
package com.fitter.service.exercise;

import com.fitter.domain.exercise.ExerciseGoal;
import com.fitter.domain.exercise.UserExerciseProgress;
import com.fitter.domain.user.User;
import com.fitter.dto.request.ExerciseGoalRequest;
import com.fitter.dto.response.ExerciseGoalResponse;
import com.fitter.exception.GoalNotFoundException;
import com.fitter.exception.UserNotFoundException;
import com.fitter.repository.exercise.ExerciseGoalRepository;
import com.fitter.repository.exercise.UserExerciseProgressRepository;
import com.fitter.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseGoalService {

    private final ExerciseGoalRepository goalRepository;
    private final UserRepository userRepository;
    private final UserExerciseProgressRepository progressRepository;

    @Transactional
    public ExerciseGoalResponse setExerciseGoal(Long userId, ExerciseGoalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        ExerciseGoal goal = ExerciseGoal.builder()
                .user(user)
                .name(request.getName())
                .targetCaloriesPerDay(request.getTargetCaloriesPerDay())
                .targetExerciseMinutesPerDay(request.getTargetExerciseMinutesPerDay())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(ExerciseGoal.GoalStatus.진행중)
                .build();

        ExerciseGoal savedGoal = goalRepository.save(goal);
        updateGoalAchievement(userId);

        return ExerciseGoalResponse.from(savedGoal, analyzeGoalAchievementByGoalId(savedGoal.getId()));
    }

    public List<ExerciseGoalResponse> getAllGoals(Long userId, LocalDate checkDate) {
        List<ExerciseGoal> goals = goalRepository.findByUserIdAndDate(userId, checkDate);
        return goals.stream()
                .map(goal -> ExerciseGoalResponse.from(goal, analyzeGoalAchievementByGoalId(goal.getId())))
                .collect(Collectors.toList());
    }

    public List<ExerciseGoalResponse> getActiveGoals(Long userId) {
        List<ExerciseGoal> activeGoals = goalRepository.findByUserIdAndStatus(userId, ExerciseGoal.GoalStatus.진행중);

        if (activeGoals.isEmpty()) {
            throw new GoalNotFoundException("No active goals found for user: " + userId);
        }

        return activeGoals.stream()
                .map(goal -> {
                    GoalAchievementAnalysis analysis = analyzeGoalAchievementByGoalId(goal.getId());
                    return ExerciseGoalResponse.from(goal, analysis);
                })
                .collect(Collectors.toList());
    }


    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        ExerciseGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalNotFoundException("Goal not found with id: " + goalId));

        // 권한 확인
        if (!goal.getUser().getId().equals(userId)) {
            throw new IllegalStateException("You don't have permission to delete this goal");
        }

        // 진행 중인 목표는 삭제할 수 없음
        /*if (goal.getStatus() == ExerciseGoal.GoalStatus.진행중) {
            throw new IllegalStateException("Cannot delete an active goal");
        }*/

        goalRepository.delete(goal);
    }

    @Transactional
    public ExerciseGoalResponse updateGoal(Long userId, Long goalId, ExerciseGoalRequest request) {
        ExerciseGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalNotFoundException("Goal not found with id: " + goalId));

        // 권한 확인
        if (!goal.getUser().getId().equals(userId)) {
            throw new IllegalStateException("You don't have permission to update this goal");
        }

        // 목표 수정
        goal.updateGoal(
                request.getName(),
                request.getTargetCaloriesPerDay(),
                request.getTargetExerciseMinutesPerDay(),
                request.getStartDate(),
                request.getEndDate()
        );

        ExerciseGoal updatedGoal = goalRepository.save(goal);
        return ExerciseGoalResponse.from(updatedGoal, analyzeGoalAchievementByGoalId(updatedGoal.getId()));
    }

    public List<Map<String, Object>> getGoalStatistics(Long userId) {
        return goalRepository.getGoalStatistics(userId);
    }

    public List<Object[]> getGoalTrends(Long userId, LocalDate startDate, LocalDate endDate) {
        return goalRepository.getGoalTrends(userId, startDate, endDate);
    }

    @Transactional
    public void completeActiveGoals(Long userId) {
        List<ExerciseGoal> activeGoals = goalRepository.findByUserIdAndStatus(userId, ExerciseGoal.GoalStatus.진행중);
        activeGoals.forEach(goal -> goal.updateStatus(ExerciseGoal.GoalStatus.완료));
    }

    public ExerciseGoalResponse getActiveGoal(Long userId) {
        return goalRepository.findByUserIdAndStatus(userId, ExerciseGoal.GoalStatus.진행중)
                .stream()
                .findFirst()
                .map(goal -> {
                    GoalAchievementAnalysis analysis = analyzeGoalAchievementByGoalId(goal.getId());
                    return ExerciseGoalResponse.from(goal, analysis);
                })
                .orElseThrow(() -> new GoalNotFoundException("No active goal found for user: " + userId));
    }

    public GoalAchievementAnalysis analyzeGoalAchievementByGoalId(Long goalId) {
        ExerciseGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalNotFoundException("Goal not found with id: " + goalId));

        LocalDate startDate = goal.getStartDate();
        LocalDate endDate = goal.getEndDate() != null ? goal.getEndDate() : LocalDate.now();

        List<UserExerciseProgress> progressList = progressRepository
                .findByUserIdAndCompletedDateBetween(goal.getUser().getId(), startDate, endDate);

        return calculateAchievementAnalysis(goal, progressList);
    }

    @Transactional
    public void updateGoalAchievement(Long userId) {
        try {
            // 활성화된 목표들 모두 조회로 변경
            List<ExerciseGoal> activeGoals = goalRepository.findByUserIdAndStatus(userId, ExerciseGoal.GoalStatus.진행중);
            if (activeGoals.isEmpty()) {
                throw new GoalNotFoundException("No active goal found");
            }

            // 오늘의 운동 기록 조회
            LocalDate today = LocalDate.now();
            List<UserExerciseProgress> todayProgress = progressRepository
                    .findByUserIdAndCompletedDate(userId, today);

            // 오늘의 총 운동 시간과 칼로리 계산
            int totalMinutes = todayProgress.stream()
                    .mapToInt(UserExerciseProgress::getActualDurationMinutes)
                    .sum();

            double totalCalories = todayProgress.stream()
                    .mapToDouble(UserExerciseProgress::getActualCaloriesBurned)
                    .sum();

            // 각 활성화된 목표에 대해 달성률 업데이트
            for (ExerciseGoal activeGoal : activeGoals) {
                // 달성률 계산
                double minutesAchievementRate = (totalMinutes * 100.0) / activeGoal.getTargetExerciseMinutesPerDay();
                double caloriesAchievementRate = (totalCalories * 100.0) / activeGoal.getTargetCaloriesPerDay();

                // 달성률 업데이트
                activeGoal.updateAchievementRates(caloriesAchievementRate, minutesAchievementRate);

                // 목표 달성 여부 체크
                if (minutesAchievementRate >= 100 && caloriesAchievementRate >= 100) {
                    activeGoal.updateStatus(ExerciseGoal.GoalStatus.완료);
                }

                goalRepository.save(activeGoal);
            }

        } catch (GoalNotFoundException e) {
            log.debug("No active goal found for user: {}", userId);
        }
    }

    private GoalAchievementAnalysis calculateAchievementAnalysis(ExerciseGoal goal, List<UserExerciseProgress> progressList) {
        Map<LocalDate, DailyProgress> dailyProgressMap = progressList.stream()
                .collect(Collectors.groupingBy(
                        UserExerciseProgress::getCompletedDate,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> new DailyProgress(
                                        list.stream().mapToDouble(UserExerciseProgress::getActualCaloriesBurned).sum(),
                                        list.stream().mapToInt(UserExerciseProgress::getActualDurationMinutes).sum()
                                )
                        )
                ));

        // 현재 날짜의 진행 상황만 계산
        DailyProgress todayProgress = dailyProgressMap.getOrDefault(LocalDate.now(),
                new DailyProgress(0.0, 0));

        // 실제 달성률 계산
        double caloriesAchievementRate = (todayProgress.getCaloriesBurned() * 100.0) / goal.getTargetCaloriesPerDay();
        double minutesAchievementRate = (todayProgress.getDurationMinutes() * 100.0) / goal.getTargetExerciseMinutesPerDay();

        return new GoalAchievementAnalysis(
                caloriesAchievementRate,
                minutesAchievementRate,
                generateAchievementFeedback(caloriesAchievementRate, minutesAchievementRate)
        );
    }

    private String generateAchievementFeedback(double caloriesAchievementRate, double minutesAchievementRate) {
        StringBuilder feedback = new StringBuilder();

        feedback.append("칼로리 소모 목표: ");
        if (caloriesAchievementRate >= 90) {
            feedback.append("목표를 잘 달성하고 있습니다! ");
        } else if (caloriesAchievementRate >= 70) {
            feedback.append("목표 달성이 양호합니다. ");
        } else {
            feedback.append("목표 달성을 위해 더 노력이 필요합니다. ");
        }

        feedback.append("운동 시간 목표: ");
        if (minutesAchievementRate >= 90) {
            feedback.append("꾸준히 운동하고 있습니다! ");
        } else if (minutesAchievementRate >= 70) {
            feedback.append("조금 더 시간을 늘려보세요. ");
        } else {
            feedback.append("운동 시간을 더 확보할 필요가 있습니다. ");
        }

        return feedback.toString();
    }

    @Transactional
    public ExerciseGoal updateGoalStatus(Long goalId, ExerciseGoal.GoalStatus status) {
        ExerciseGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalNotFoundException("Goal not found with id: " + goalId));

        goal.updateStatus(status);
        return goal;
    }

    @lombok.Value
    public static class DailyProgress {
        double caloriesBurned;
        int durationMinutes;
    }

    @lombok.Value
    public static class GoalAchievementAnalysis {
        double caloriesAchievementRate;
        double minutesAchievementRate;
        String feedback;
    }



}