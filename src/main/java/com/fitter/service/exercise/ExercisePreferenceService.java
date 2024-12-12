// ExercisePreferenceService.java
package com.fitter.service.exercise;

import com.fitter.domain.exercise.ExerciseBase;
import com.fitter.domain.exercise.ExercisePreference;
import com.fitter.domain.user.User;
import com.fitter.dto.request.ExercisePreferenceRequest;
import com.fitter.dto.response.ExercisePreferenceResponse;
import com.fitter.exception.UserNotFoundException;
import com.fitter.repository.exercise.ExercisePreferenceRepository;
import com.fitter.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExercisePreferenceService {

    private final ExercisePreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    private ExerciseBase.ExerciseCategory determineCategory(ExerciseBase.BodyPart bodyPart) {
        return switch (bodyPart) {
            case 가슴, 어깨, 등, 이두, 삼두, 전완근 -> ExerciseBase.ExerciseCategory.상체근력운동;
            case 하체 -> ExerciseBase.ExerciseCategory.하체근력운동;
            case 코어 -> ExerciseBase.ExerciseCategory.코어강화운동;
            case 유산소 -> ExerciseBase.ExerciseCategory.유산소운동;
            case 스포츠 -> ExerciseBase.ExerciseCategory.전신강화운동;
        };
    }

    @Transactional
    public ExercisePreferenceResponse setPreference(Long userId, ExercisePreferenceRequest request) {
        // 이미 선호도가 설정되어 있는지 확인
        if (preferenceRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("이미 운동 선호도가 설정되어 있습니다. 수정을 원하시면 PUT 메서드를 사용해주세요.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        ExercisePreference preference = ExercisePreference.builder()
                .user(user)
                .preferredBodyPart(request.getPreferredBodyPart())
                .preferredCategory(determineCategory(request.getPreferredBodyPart()))
                .preferredIntensity(request.getPreferredIntensity())
                .preferredTimeOfDay(request.getPreferredTimeOfDay())
                .build();

        return ExercisePreferenceResponse.from(preferenceRepository.save(preference));
    }

    public ExercisePreferenceResponse getPreference(Long userId) {
        ExercisePreference preference = preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("설정된 운동 선호도가 없습니다."));

        return ExercisePreferenceResponse.from(preference);
    }

    @Transactional
    public ExercisePreferenceResponse updatePreference(Long userId, ExercisePreferenceRequest request) {
        // 기존 선호도 조회
        ExercisePreference preference = preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("수정할 운동 선호도가 없습니다."));

        // 기존 객체 업데이트
        preference.updatePreferredBodyPart(request.getPreferredBodyPart());
        preference.updatePreferredCategory(determineCategory(request.getPreferredBodyPart()));
        preference.updatePreferredIntensity(request.getPreferredIntensity());
        preference.updatePreferredTimeOfDay(request.getPreferredTimeOfDay());

        // 변경된 엔티티 저장 및 반환
        return ExercisePreferenceResponse.from(preferenceRepository.save(preference));
    }
}