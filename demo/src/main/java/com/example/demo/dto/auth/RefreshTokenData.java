package com.example.demo.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Refresh token data stored in Redis
 * Optimized structure for Redis storage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenData {
    
    private Long userId;
    private String email;
    private String role;
    private String deviceId;
    private Long issuedAt;
    
    /**
     * Create from token metadata
     */
    public static RefreshTokenData from(TokenMetadata metadata) {
        return RefreshTokenData.builder()
                .userId(metadata.getUserId())
                .email(metadata.getEmail())
                .role(metadata.getRole())
                .deviceId(metadata.getDeviceId())
                .issuedAt(metadata.getIssuedAt().getTime())
                .build();
    }
}
