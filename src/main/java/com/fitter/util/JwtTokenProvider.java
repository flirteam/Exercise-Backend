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
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value; // 환경 변수 주입을 위한 @Value
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.refreshSecret}")
    private String jwtRefreshSecret;

    @Value("${jwt.expiresIn}")
    private long jwtExpiresIn;

    @Value("${jwt.refreshExpiresIn}")
    private long jwtRefreshExpiresIn;

    // 시크릿 키 생성 메서드 추가
    private Key getSigningKey(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Access Token 생성
    public String generateAccessToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiresIn * 1000))
                .signWith(getSigningKey(jwtSecret))
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtRefreshExpiresIn * 1000))
                .signWith(getSigningKey(jwtRefreshSecret))
                .compact();
    }

    // Access Token과 Refresh Token을 함께 생성
    public Tokens generateTokens(Long userId) {
        String accessToken = generateAccessToken(userId);
        String refreshToken = generateRefreshToken(userId);
        return new Tokens(accessToken, refreshToken);
    }

    // 기본 토큰 생성 (AccessToken 생성)
    public String generateToken(Long userId) {
        return generateAccessToken(userId);
    }

    public Long getUserIdFromToken(String token) {
        return getUserIdFromToken(token, false);
    }

    // 토큰에서 사용자 ID 추출
    public Long getUserIdFromToken(String token, boolean isRefreshToken) {
        String secret = isRefreshToken ? jwtRefreshSecret : jwtSecret;
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey(secret))
                .build()
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token, boolean isRefreshToken) {
        try {
            String secret = isRefreshToken ? jwtRefreshSecret : jwtSecret;
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(secret))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰 응답을 위한 내부 클래스
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