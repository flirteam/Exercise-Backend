//JwtTokenProvider.java
package com.fitter.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

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

    // Secret Key 생성 메서드
    private Key getSigningKey(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Long userId) {
        return Jwts.builder()
                .setClaims(Map.of("id", userId))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiresIn * 1000))
                .signWith(getSigningKey(jwtSecret), SignatureAlgorithm.HS384)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setClaims(Map.of("id", userId))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiresIn * 1000))
                .signWith(getSigningKey(jwtRefreshSecret), SignatureAlgorithm.HS384)
                .compact();
    }

    // Access Token 생성 (기본 메서드)
    public String generateToken(Long userId) {
        return generateAccessToken(userId);
    }


    // 토큰에서 사용자 ID 추출
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey(jwtSecret))
                .build()
                .parseClaimsJws(token.replace("Bearer ", "")) // Bearer 제거
                .getBody();
        return Long.parseLong(claims.get("id").toString());
    }


    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(jwtSecret))
                    .build()
                    .parseClaimsJws(token); // 토큰 파싱 및 검증
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
