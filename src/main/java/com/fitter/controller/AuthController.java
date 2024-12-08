//AuthController.java
package com.fitter.controller;

import com.fitter.dto.request.UserRegistrationRequest;
import com.fitter.dto.request.LoginRequest;
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

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String sessionId = userService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(sessionId);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String sessionId) {
        userService.logout(sessionId);
        return ResponseEntity.noContent().build();
    }
}
