package com.example.demo.service.impl;

import com.example.demo.constants.SecurityConstants;
import com.example.demo.dto.auth.AuthResponse;
import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.TokenPair;
import com.example.demo.dto.user.CreateUserRequest;
import com.example.demo.entity.UserEntity;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.payload.ApiResponse;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.AuthService;
import com.example.demo.custom.CustomUserDetails;
import com.example.demo.auth.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AuthService Implementation - Refactored Version
 * 
 * IMPROVEMENTS:
 * ✅ Using new TokenService for better separation of concerns
 * ✅ Cleaner cookie management
 * ✅ Better error handling with custom exceptions
 * ✅ Simplified logic
 * ✅ Using constants instead of magic strings
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class  AuthServiceImpl implements AuthService {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // =========================
    // 🔐 LOGIN
    // =========================
    @Override
    public ResponseEntity<ApiResponse<AuthResponse>> login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String role = userDetails.getAuthorities().iterator().next().getAuthority();
            Long userId = userDetails.getUserId();

            // Generate token pair
            TokenPair tokenPair = tokenService.generateTokenPair(userId, userDetails.getUsername(), role);

            // Build response
            AuthResponse response = AuthResponse.builder()
                    .email(userDetails.getUsername())
                    .role(role)
                    .accessToken(tokenPair.getAccessToken())
                    .expiresIn(tokenPair.getAccessTokenExpiresIn())
                    .build();

            // Set refresh token cookie
            ResponseCookie cookie = buildRefreshTokenCookie(
                    tokenPair.getRefreshToken(), 
                    tokenPair.getRefreshTokenExpiresIn()
            );

            log.info("User logged in successfully: userId={}", userId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(ApiResponse.success("Login successful", response));

        } catch (BadCredentialsException e) {
            log.warn("Login failed: invalid credentials for email={}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email or password"));
        } catch (DisabledException e) {
            log.warn("Login failed: account disabled for email={}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Account is disabled"));
        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    // =========================
    // 👤 REGISTER
    // =========================
    @Override
    public ResponseEntity<ApiResponse<String>> register(CreateUserRequest dto) {
        try {
            // Check if email already exists
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("Email already exists"));
            }

            // Create user
            UserEntity user = userMapper.toEntity(dto);
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            userRepository.save(user);

            log.info("User registered successfully: email={}", dto.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully"));

        } catch (Exception e) {
            log.error("Registration error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    // =========================
    // ♻️ REFRESH TOKEN
    // =========================
    @Override
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(String refreshToken, String authHeader) {
        try {
            // Validate refresh token
            if (!StringUtils.hasText(refreshToken)) {
                throw new UnauthorizedException("Refresh token is missing");
            }

            // Extract old access token
            String oldAccessToken = extractAccessToken(authHeader);

            // Refresh token pair
            TokenPair tokenPair = tokenService.refreshAccessToken(refreshToken, oldAccessToken);

            // Extract metadata from new access token
            var metadata = tokenProvider.extractTokenMetadata(tokenPair.getAccessToken());

            // Build response
            AuthResponse response = AuthResponse.builder()
                    .email(metadata.getEmail())
                    .role(metadata.getRole())
                    .accessToken(tokenPair.getAccessToken())
                    .expiresIn(tokenPair.getAccessTokenExpiresIn())
                    .build();

            log.info("Token refreshed successfully for userId={}", metadata.getUserId());

            return ResponseEntity.ok()
                    .body(ApiResponse.success("Token refreshed successfully", response));

        } catch (UnauthorizedException e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Token refresh error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Token refresh failed: " + e.getMessage()));
        }
    }

    // =========================
    // 🚪 LOGOUT
    // =========================
    @Override
    public ResponseEntity<ApiResponse<String>> logout(String refreshToken, String authHeader) {
        try {
            if (StringUtils.hasText(refreshToken)) {
                // Extract metadata
                var metadata = tokenProvider.extractTokenMetadata(refreshToken);
                System.out.println("Logging out userId: " + metadata.getUserId() + ", deviceId: " + metadata.getDeviceId());
                // Extract access token
                String accessToken = extractAccessToken(authHeader);

                // Revoke tokens
                tokenService.revokeTokens(metadata.getUserId(), metadata.getDeviceId(), accessToken);

                log.info("User logged out successfully: userId={}", metadata.getUserId());
            }

            // Clear refresh token cookie
            ResponseCookie cookie = buildRefreshTokenCookie(null, 0);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(ApiResponse.success("Logged out successfully"));

        } catch (Exception e) {
            log.error("Logout error", e);
            // Even if error occurs, clear cookie and return success
            ResponseCookie cookie = buildRefreshTokenCookie(null, 0);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(ApiResponse.success("Logged out successfully"));
        }
    }

    // =========================
    // �️ HELPER METHODS
    // =========================

    /**
     * Build refresh token cookie
     */
    private ResponseCookie buildRefreshTokenCookie(String refreshToken, long maxAgeMillis) {
        return ResponseCookie.from(SecurityConstants.COOKIE_REFRESH_TOKEN, refreshToken != null ? refreshToken : "")
                .httpOnly(true)
                .secure(false) // ⚠️ Set to false for localhost HTTP (true for production HTTPS)
                .sameSite("Lax") // ✅ Changed from Strict to Lax - allows same-site requests
                .path("/")
                .maxAge(maxAgeMillis / 1000) // Convert to seconds
                .build();
    }

    /**
     * Extract access token from Authorization header
     */
    private String extractAccessToken(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return authHeader.substring(SecurityConstants.TOKEN_PREFIX.length());
        }
        return null;
    }
}

