package com.example.demo.dto.auth;

import com.example.demo.entity.enums.TokenType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Token metadata extracted from JWT
 * Contains all information about a token
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenMetadata {
    
    private String tokenId;
    private Long userId;
    private String email;
    private String role;
    private String deviceId;
    private TokenType tokenType;
    private Date issuedAt;
    private Date expiresAt;
    
    /**
     * Check if token is expired
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.before(new Date());
    }
    
    /**
     * Get remaining validity in milliseconds
     */
    public long getRemainingValidity() {
        if (expiresAt == null) {
            return 0;
        }
        long remaining = expiresAt.getTime() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
}
