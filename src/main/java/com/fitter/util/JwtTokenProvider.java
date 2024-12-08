//JwtTokenProvider.java
package com.fitter.util;

/*import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512); // 강력한 키 생성
    private final long expirationTime = 86400000; // 토큰 유효 시간 (1일)

    // JWT 생성
    public String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // 사용자 ID를 subject로 설정
                .setIssuedAt(new Date()) // 토큰 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // 만료 시간
                .signWith(secretKey) // 서명 키 사용
                .compact();
    }

    // JWT에서 사용자 ID 추출
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey) // 서명 키로 토큰 파싱
                .build()
                .parseClaimsJws(token.replace("Bearer ", "")) // Bearer 접두어 제거
                .getBody();
        return Long.parseLong(claims.getSubject()); // subject에서 사용자 ID 추출
    }

    // JWT 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 서명 키 설정
                    .build()
                    .parseClaimsJws(token); // 토큰 파싱 및 검증
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}*/

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value; // 환경 변수 주입을 위한 @Value
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Node.js 환경 변수와 동기화: Access Token Secret
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Node.js 환경 변수와 동기화: Refresh Token Secret
    @Value("${jwt.refreshSecret}")
    private String jwtRefreshSecret;

    // Node.js 환경 변수와 동기화: Access Token 유효 기간 (초 단위)
    @Value("${jwt.expiresIn}")
    private long jwtExpiresIn;

    // Node.js 환경 변수와 동기화: Refresh Token 유효 기간 (초 단위)
    @Value("${jwt.refreshExpiresIn}")
    private long jwtRefreshExpiresIn;

    // **변경 1: Access Token 생성 메서드 추가**
    // Node.js generateTokens에서 Access Token 생성 로직과 동일하게 구현
    public String generateAccessToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // 사용자 ID를 subject로 설정
                .setIssuedAt(new Date()) // 발급 시간 설정
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiresIn * 1000)) // 만료 시간 설정 (초 → 밀리초 변환)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())) // Access Token 서명
                .compact();
    }

    // **변경 2: Refresh Token 생성 메서드 추가**
    // Node.js generateTokens에서 Refresh Token 생성 로직과 동일하게 구현
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // 사용자 ID를 subject로 설정
                .setIssuedAt(new Date()) // 발급 시간 설정
                .setExpiration(new Date(System.currentTimeMillis() + jwtRefreshExpiresIn * 1000)) // 만료 시간 설정 (초 → 밀리초 변환)
                .signWith(Keys.hmacShaKeyFor(jwtRefreshSecret.getBytes())) // Refresh Token 서명
                .compact();
    }

    // **변경 3: Access Token과 Refresh Token을 함께 반환**
    // Node.js의 generateTokens 함수와 동일하게 두 개의 토큰을 묶어 반환
    public Tokens generateTokens(Long userId) {
        String accessToken = generateAccessToken(userId); // Access Token 생성
        String refreshToken = generateRefreshToken(userId); // Refresh Token 생성
        return new Tokens(accessToken, refreshToken); // 두 토큰을 묶어서 반환
    }

    // **변경 4: Access Token/Refresh Token 구분하여 사용자 ID 추출**
    // 토큰 타입에 따라 올바른 서명 키를 사용
    // **오버로딩된 메서드: Access Token 처리 기본**
    public String generateToken(Long userId) {
        return generateAccessToken(userId);
    }

    public Long getUserIdFromToken(String token) {
        return getUserIdFromToken(token, false); // 기본적으로 Access Token 처리
    }

    // 기존 메서드: Access Token 또는 Refresh Token 구분
    public Long getUserIdFromToken(String token, boolean isRefreshToken) {
        String secret = isRefreshToken ? jwtRefreshSecret : jwtSecret; // Secret 결정
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes())) // 적절한 Secret으로 서명 검증
                .build()
                .parseClaimsJws(token.replace("Bearer ", "")) // Bearer 접두어 제거
                .getBody();
        return Long.parseLong(claims.getSubject()); // subject에서 사용자 ID 추출
    }

    // **변경 5: Access Token/Refresh Token 구분하여 유효성 검증**
    public boolean validateToken(String token, boolean isRefreshToken) {
        try {
            String secret = isRefreshToken ? jwtRefreshSecret : jwtSecret; // Refresh Token 여부에 따라 Secret 결정
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes())) // 적절한 Secret으로 서명 검증
                    .build()
                    .parseClaimsJws(token); // 토큰 파싱 및 검증
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // 유효하지 않은 경우 false 반환
        }
    }

    // **변경 6: Tokens 클래스 추가**
    // Access Token과 Refresh Token을 묶어 반환하는 구조체 역할
    public static class Tokens {
        private final String accessToken;
        private final String refreshToken;

        public Tokens(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }
}