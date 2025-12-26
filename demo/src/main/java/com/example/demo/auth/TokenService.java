package com.example.demo.auth;

import com.example.demo.dto.auth.RefreshTokenData;
import com.example.demo.dto.auth.TokenMetadata;
import com.example.demo.dto.auth.TokenPair;
import com.example.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * TokenService - High-level token management
 * Orchestrates JwtTokenProvider, RefreshTokenService, and TokenBlacklistService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService blacklistService;

    /**
     * Generate new token pair for user login
     */
    public TokenPair generateTokenPair(Long userId, String email, String role) {
        String deviceId = UUID.randomUUID().toString();
        return generateTokenPair(userId, email, role, deviceId);
    }

    /**
     * Generate new token pair with specific device ID
     */
    public TokenPair generateTokenPair(Long userId, String email, String role, String deviceId) {
        // Check device limit
        refreshTokenService.enforceMaxDevices(userId);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(userId, email, role, deviceId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId, email, role, deviceId);

        // Extract metadata for storage
        TokenMetadata refreshMetadata = jwtTokenProvider.extractTokenMetadata(refreshToken);
        RefreshTokenData tokenData = RefreshTokenData.from(refreshMetadata);

        // Store refresh token in Redis
        refreshTokenService.saveRefreshToken(
            userId, 
            deviceId, 
            tokenData, 
            jwtTokenProvider.getRefreshTokenExpiration()
        );

        log.info("Generated token pair for userId={}, deviceId={}", userId, deviceId);

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .refreshTokenExpiresIn(jwtTokenProvider.getRefreshTokenExpiration())
                .build();
    }

    /**
     * Refresh access token using refresh token
     * Invalidates old access token
     */
    public TokenPair refreshAccessToken(String refreshToken, String oldAccessToken) {
        // Validate and extract refresh token metadata
        TokenMetadata refreshMetadata = jwtTokenProvider.extractTokenMetadata(refreshToken);
        
        // Verify refresh token exists in Redis
        if (!refreshTokenService.existsRefreshToken(refreshMetadata.getUserId(), refreshMetadata.getDeviceId())) {
            throw new IllegalStateException("Refresh token not found in storage");
        }

        // ⚠️ KHÔNG blacklist token cũ ngay - để client có thời gian chuyển sang token mới
        // Token cũ sẽ tự expire sau vài phút
        // if (oldAccessToken != null) {
        //     blacklistAccessToken(oldAccessToken);
        // }

        // Generate new access token (keep same refresh token)
        String newAccessToken = jwtTokenProvider.generateAccessToken(
            refreshMetadata.getUserId(),
            refreshMetadata.getEmail(),
            refreshMetadata.getRole(),
            refreshMetadata.getDeviceId()
        );

        log.info("Refreshed access token for userId={}", refreshMetadata.getUserId());

        return TokenPair.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Same refresh token
                .accessTokenExpiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .refreshTokenExpiresIn(refreshMetadata.getRemainingValidity())
                .build();
    }

    /**
     * Revoke tokens (logout)
     */
    public void revokeTokens(Long userId, String deviceId, String accessToken) {
        // Delete refresh token
        refreshTokenService.deleteRefreshToken(userId, deviceId);

        // Blacklist access token
        if (accessToken != null) {
            blacklistAccessToken(accessToken);
        }

        log.info("Revoked tokens for userId={}, deviceId={}", userId, deviceId);
    }

    /**
     * Logout from all devices
     */
    public void revokeAllTokens(Long userId) {
        refreshTokenService.deleteAllRefreshTokensForUser(userId);
        log.info("Revoked all tokens for userId={}", userId);
    }

    /**
     * Blacklist access token
     */
    public void blacklistAccessToken(String accessToken) {
        try {
            TokenMetadata metadata = jwtTokenProvider.extractTokenMetadata(accessToken);
            System.out.println("metadata: " + metadata);
            long remainingValidity = metadata.getRemainingValidity();
            
            if (remainingValidity > 0) {
                blacklistService.blacklistToken(metadata.getTokenId(), remainingValidity);
            }
        } catch (Exception e) {
            log.debug("Cannot blacklist token: {}", e.getMessage());
        }
    }

    /**
     * Validate access token (check signature, expiration, and blacklist)
     */
    public boolean validateAccessToken(String accessToken) {
        try {
            // Validate signature and expiration
            if (!jwtTokenProvider.validateToken(accessToken)) {
                return false;
            }

            // Check blacklist
            TokenMetadata metadata = jwtTokenProvider.extractTokenMetadata(accessToken);
            return !blacklistService.isBlacklisted(metadata.getTokenId());
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
