package com.example.demo.service;

import com.example.demo.dto.auth.AuthResponse;
import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.user.CreateUserRequest;
import com.example.demo.payload.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<ApiResponse<AuthResponse>> login(LoginRequest request);

    ResponseEntity<ApiResponse<String>> register(CreateUserRequest request);

    ResponseEntity<ApiResponse<AuthResponse>> refresh(String refreshToken, String authHeader);

    ResponseEntity<ApiResponse<String>> logout(String refreshToken, String authHeader);
}
