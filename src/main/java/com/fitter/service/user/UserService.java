//UserService.java
package com.fitter.service.user;

import com.fitter.domain.user.User;
import com.fitter.domain.user.UserPhysicalInfo;
import com.fitter.dto.request.PhysicalInfoRequest;
import com.fitter.dto.request.UserRegistrationRequest;
import com.fitter.dto.response.UserPhysicalInfoResponse;
import com.fitter.dto.response.UserResponse;
import com.fitter.exception.UserNotFoundException;
import com.fitter.repository.user.UserRepository;
import com.fitter.repository.user.UserPhysicalInfoRepository;
import com.fitter.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserPhysicalInfoRepository physicalInfoRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입 처리
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        if (!request.getPassword().equals(request.getPassword2())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 사용자 기본 정보 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(hashPassword(request.getPassword()))
                .username(request.getUsername())
                .birthdate(request.getBirthdate())
                .build();

        // 신체 정보 초기값 설정
        UserPhysicalInfo physicalInfo = UserPhysicalInfo.builder()
                .user(user)  // user 설정 추가
                .currentWeight(0.0)
                .targetWeight(0.0)
                .height(0.0)
                .age(0)
                .gender(UserPhysicalInfo.Gender.Male)
                .activityLevel(1)
                .goalType(UserPhysicalInfo.GoalType.균형_식단)
                .basalMetabolicRate(0.0)    // 초기값 설정
                .activeMetabolicRate(0.0)   // 초기값 설정
                .bmi(0.0)                   // 초기값 설정
                .bodyFatPercentage(0.0)     // 초기값 설정
                .build();

        user.setPhysicalInfo(physicalInfo);

        return UserResponse.from(userRepository.save(user));
    }

    // 로그인 처리 및 JWT 반환
    @Transactional
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("이메일 또는 비밀번호가 잘못되었습니다."));

        if (!checkPassword(password, user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다.");
        }

        // JWT 토큰 생성
        return jwtTokenProvider.generateToken(user.getId());
    }

    // 로그아웃 처리 (Redis 세션 제거)
    public void logout(String sessionId) {
        redisTemplate.delete(sessionId);
    }

    // Redis 세션을 통해 사용자 조회
    public UserResponse getUserBySession(String sessionId) {
        String userId = redisTemplate.opsForValue().get(sessionId);
        if (userId == null) {
            throw new IllegalArgumentException("유효하지 않은 세션입니다.");
        }

        return getUser(Long.parseLong(userId));
    }

    // 사용자 ID로 사용자 정보 조회
    public UserResponse getUser(Long userId) {
        User user = findUserById(userId);
        return UserResponse.from(user);
    }

    // 비밀번호 해싱 (BCrypt 사용)
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // 비밀번호 검증
    private boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    // 전체 사용자 목록 조회 (페이징 처리)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Transactional
    public UserResponse addUserInfo(Long userId, PhysicalInfoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 기존 신체 정보가 있는지 확인
        UserPhysicalInfo existingInfo = user.getPhysicalInfo();
        if (existingInfo != null) {
            // 기존 정보가 있으면 업데이트
            existingInfo.updatePhysicalInfo(
                    request.getAge(),
                    request.getHeight(),
                    request.getCurrentWeight(),
                    request.getActivityLevel(),
                    request.getTargetWeight(),
                    request.getGender(),
                    request.getGoalType()
            );
        } else {
            // 새로운 신체 정보 생성
            UserPhysicalInfo physicalInfo = UserPhysicalInfo.builder()
                    .currentWeight(request.getCurrentWeight())
                    .targetWeight(request.getTargetWeight())
                    .height(request.getHeight())
                    .age(request.getAge())
                    .gender(request.getGender())
                    .activityLevel(request.getActivityLevel())
                    .goalType(request.getGoalType())
                    .build();

            user.setPhysicalInfo(physicalInfo);
        }

        return UserResponse.from(userRepository.save(user));
    }

    public UserPhysicalInfoResponse getUserPhysicalInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return UserPhysicalInfoResponse.from(user.getPhysicalInfo());
    }

    // UserService.java의 updateUserPhysicalInfo 메소드를 수정
    @Transactional
    public UserPhysicalInfoResponse updateUserPhysicalInfo(Long userId, PhysicalInfoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        UserPhysicalInfo physicalInfo = user.getPhysicalInfo();
        physicalInfo.updatePhysicalInfo(
                request.getAge(),
                request.getHeight(),
                request.getCurrentWeight(),
                request.getActivityLevel(),
                request.getTargetWeight(),
                request.getGender(),
                request.getGoalType()
        );

        return UserPhysicalInfoResponse.from(physicalInfo);
    }

    // 사용자 정보 업데이트
    @Transactional
    public UserResponse updateUser(Long userId, UserRegistrationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 기본 사용자 정보만 업데이트
        user.updateName(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            // 비밀번호 일치 검사
            if (!request.getPassword().equals(request.getPassword2())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
            user.updatePassword(hashPassword(request.getPassword()));
        }

        return UserResponse.from(user);
    }


    @Transactional
    public UserResponse updatePhysicalInfo(Long userId, PhysicalInfoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        UserPhysicalInfo physicalInfo = user.getPhysicalInfo();
        physicalInfo.updatePhysicalInfo(
                request.getAge(),
                request.getHeight(),
                request.getCurrentWeight(),
                request.getActivityLevel(),
                request.getTargetWeight(),
                request.getGender(),     // 추가
                request.getGoalType()
        );

        return UserResponse.from(user);
    }

    // 사용자 삭제
    @Transactional
    public void deleteUser(Long userId) {
        User user = findUserById(userId);
        userRepository.delete(user);
    }

    // 활동 레벨별 사용자 목록 조회
    public List<UserResponse> getUsersByActivityLevel(Integer activityLevel) {
        return userRepository.findByActivityLevel(activityLevel).stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    // BMI 범위별 사용자 목록 조회
    public List<UserResponse> getUsersByBmiRange(Double minBmi, Double maxBmi) {
        return userRepository.findByBmiRange(minBmi, maxBmi).stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    // 목표 체중에 도달한 사용자 목록 조회
    public List<UserResponse> getUsersReachedTargetWeight(Double threshold) {
        return userRepository.findUsersReachedTargetWeight(threshold).stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    // 특정 사용자의 운동 루틴 개수 조회
    public Long getExerciseRoutineCount(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("사용자를 찾을 수 없습니다.");
        }
        return userRepository.countExerciseRoutines(userId);
    }

    // 내부 메서드: 사용자 조회
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }
}
