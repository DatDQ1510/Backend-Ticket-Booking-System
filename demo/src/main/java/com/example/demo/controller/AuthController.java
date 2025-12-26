package com.example.demo.controller;

import com.example.demo.dto.auth.AuthResponse;
import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.user.CreateUserRequest;
import com.example.demo.payload.ApiResponse;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // =========================
    // 🔐 LOGIN
    // =========================
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // =========================
    // 👤 REGISTER
    // =========================
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody CreateUserRequest request) {
        return authService.register(request);
    }

    // =========================
    // ♻️ REFRESH TOKEN
    // =========================
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        return authService.refresh(refreshToken, authHeader);
    }

    // =========================
    // 🚪 LOGOUT
    // =========================
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        System.out.println("Logout called with refreshToken: " + refreshToken);
        System.out.println("Authorization header: " + authHeader);
        return authService.logout(refreshToken, authHeader);
    }

}
