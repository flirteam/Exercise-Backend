//AuthController.java
package com.fitter.controller;

import com.fitter.dto.request.UserRegistrationRequest;
import com.fitter.dto.request.LoginRequest;
import com.fitter.dto.response.LoginResponse;
import com.fitter.dto.response.UserResponse;
import com.fitter.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor

public class AuthController {

    private final UserService userService;

    // AuthController.java
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // 이메일과 비밀번호로 로그인 처리 후 토큰과 사용자 정보 받기
        LoginResponse response = userService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        // Bearer 토큰 처리
        String actualToken = token.replace("Bearer ", "");
        userService.logout(actualToken);
        return ResponseEntity.noContent().build();
    }
}
