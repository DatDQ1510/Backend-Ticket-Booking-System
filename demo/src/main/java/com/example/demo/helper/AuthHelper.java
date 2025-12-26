package com.example.demo.helper;

import com.example.demo.dto.auth.AuthResponse;
import com.example.demo.payload.ApiResponse;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.auth.TokenRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthHelper {

    private final JwtTokenProvider tokenProvider;
    private final TokenRedisService redisService;

    // =========================
    // 🍪 COOKIE BUILDER
    // =========================
    public ResponseCookie buildCookie(String refreshToken, long maxAgeSeconds) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(maxAgeSeconds)  // 0 = xóa cookie
                .build();
    }

    // =========================
    // 🗑️ INVALIDATE ACCESS TOKEN
    // =========================
    public void invalidateAccessToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            if (tokenProvider.validateToken(accessToken)) {
                String deviceId = tokenProvider.getDeviceIdFromToken(accessToken);
                redisService.delete(buildAccessKey(deviceId, accessToken));
            }
        }
    }

    // =========================
    // ✅ VALIDATE REFRESH TOKEN
    // =========================
    public void validateRefreshToken(String token) {
        if (token == null || !tokenProvider.validateToken(token)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }
    }

    // =========================
    // 🔑 REDIS KEYS
    // =========================
    public String buildAccessKey(String deviceId, String token) {
        return String.format("auth:access:%s:%s", deviceId, token);
    }

    public String buildRefreshKey(String deviceId, String token) {
        return String.format("auth:refresh:%s:%s", deviceId, token);
    }

    // =========================
    // 🚫 UNAUTHORIZED RESPONSE
    // =========================
    public ResponseEntity<ApiResponse<AuthResponse>> unauthorized(String msg) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(msg));
    }
}
