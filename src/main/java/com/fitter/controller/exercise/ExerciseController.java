    //ExerciseController.java exercise
    package com.fitter.controller.exercise;

    import com.fitter.domain.exercise.ExerciseBase;
    import com.fitter.dto.request.ExerciseRoutineRequest;
    import com.fitter.dto.request.ExerciseGoalRequest;
    import com.fitter.dto.response.ExerciseDetailResponse;
    import com.fitter.dto.response.ExerciseRoutineResponse;
    import com.fitter.service.exercise.ExerciseService;
    import com.fitter.service.exercise.ExerciseGoalService;
    import com.fitter.util.JwtTokenProvider;
    import jakarta.transaction.Transactional;
    import lombok.RequiredArgsConstructor;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import jakarta.validation.Valid;
    import java.time.LocalDate;
    import java.util.List;

    @RestController
    @RequestMapping("/api/exercises")
    @RequiredArgsConstructor
    public class ExerciseController {

        private final ExerciseService exerciseService;
        private final ExerciseGoalService exerciseGoalService;
        private final JwtTokenProvider jwtTokenProvider; // JWT 유틸리티 클래스

        @PostMapping("/routines")
        public ResponseEntity<ExerciseRoutineResponse> createExerciseRoutine(
                @RequestHeader("Authorization") String token,
                @Valid @RequestBody ExerciseRoutineRequest request) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            ExerciseRoutineResponse response = exerciseService.createExerciseRoutine(userId, request);
            return ResponseEntity.ok(response);
        }

        @GetMapping("/routines")
        public ResponseEntity<List<ExerciseRoutineResponse>> getUserExerciseRoutines(
                @RequestHeader("Authorization") String token,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            List<ExerciseRoutineResponse> routines = exerciseService.getUserExerciseRoutines(userId, startDate, endDate);
            return ResponseEntity.ok(routines);
        }

        @GetMapping("/routines/{routineId}")
        public ResponseEntity<ExerciseRoutineResponse> getExerciseRoutine(
                @PathVariable Long routineId) {
            // TODO: Implement getExerciseRoutine method in service
            return ResponseEntity.ok().build();
        }

        @PutMapping("/routines/{routineId}")
        public ResponseEntity<ExerciseRoutineResponse> updateExerciseRoutine(
                @PathVariable Long routineId,
                @Valid @RequestBody ExerciseRoutineRequest request) {
            ExerciseRoutineResponse response = exerciseService.updateExerciseRoutine(routineId, request);
            return ResponseEntity.ok(response);  // 수정된 결과를 반환하도록 변경
        }

        @DeleteMapping("/routines/{routineId}")
        @Transactional
        public ResponseEntity<Void> deleteExerciseRoutine(@PathVariable Long routineId) {
            exerciseService.deleteExerciseRoutine(routineId);
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/category/{category}")
        public ResponseEntity<List<ExerciseDetailResponse>> getExercisesByCategory(
                @PathVariable ExerciseBase.ExerciseCategory category) {
            return ResponseEntity.ok(exerciseService.getExercisesByCategory(category));
        }

        @GetMapping("/search")
        public ResponseEntity<List<ExerciseDetailResponse>> searchExercises(
                @RequestParam String keyword) {
            return ResponseEntity.ok(exerciseService.searchExercises(keyword));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ExerciseDetailResponse> getExerciseDetail(
                @PathVariable Long id) {
            return ResponseEntity.ok(exerciseService.getExerciseDetail(id));
        }

        @GetMapping("/bodypart/{bodyPart}")
        public ResponseEntity<List<ExerciseDetailResponse>> getExercisesByBodyPart(
                @PathVariable ExerciseBase.BodyPart bodyPart) {
            return ResponseEntity.ok(exerciseService.getExercisesByBodyPart(bodyPart));
        }

    }
