package com.example.demo.security;

import com.example.demo.constants.ErrorCode;
import com.example.demo.constants.SecurityConstants;
import com.example.demo.dto.auth.TokenMetadata;
import com.example.demo.entity.enums.TokenType;
import com.example.demo.exception.TokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT Token Provider - Refactored Version
 * 
 * CHANGES:
 * ✅ Fixed bug: now using duration instead of absolute time
 * ✅ Added role to claims
 * ✅ Using modern API (parserBuilder instead of deprecated parser)
 * ✅ Optimized: parse token only once
 * ✅ Added TokenMetadata for better structure
 * ✅ Better error handling with custom exceptions
 * ✅ Using constants instead of magic strings
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey jwtSecret;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret) {
        this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // =========================
    // TOKEN GENERATION
    // =========================

    /**
     * Generate JWT token with all claims
     * FIXED: Now correctly uses duration, not absolute time
     */
    private String generateToken(Long userId, String email, String role, String deviceId, 
                                 TokenType tokenType, long durationMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + durationMillis);

        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.CLAIM_USER_ID, userId);
        claims.put(SecurityConstants.CLAIM_ROLE, role); // ✅ NOW INCLUDES ROLE!
        claims.put(SecurityConstants.CLAIM_DEVICE_ID, deviceId);
        claims.put(SecurityConstants.CLAIM_TOKEN_TYPE, tokenType.name());

        return Jwts .builder()
                .setId(UUID.randomUUID().toString())
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecret, SignatureAlgorithm.HS512) // ✅ Modern API
                .compact();
    }

    /**
     * Generate access token (15 minutes)
     */
    public String generateAccessToken(Long userId, String email, String role, String deviceId) {
        return generateToken(userId, email, role, deviceId, 
                           TokenType.ACCESS, SecurityConstants.ACCESS_TOKEN_VALIDITY);
    }

    /**
     * Generate refresh token (7 days)
     */
    public String generateRefreshToken(Long userId, String email, String role, String deviceId) {
        return generateToken(userId, email, role, deviceId, 
                           TokenType.REFRESH, SecurityConstants.REFRESH_TOKEN_VALIDITY);
    }

    // =========================
    // TOKEN PARSING & VALIDATION
    // =========================

    /**
     * Extract all claims from token
     * ✅ Using modern parserBuilder API
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            throw new TokenException(ErrorCode.TOKEN_EXPIRED, "Token has expired");
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            throw new TokenException(ErrorCode.TOKEN_INVALID, "Unsupported JWT token");
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            throw new TokenException(ErrorCode.TOKEN_INVALID, "Malformed JWT token");
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            throw new TokenException(ErrorCode.TOKEN_INVALID, "Invalid JWT signature");
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            throw new TokenException(ErrorCode.TOKEN_INVALID, "JWT claims string is empty");
        }
    }

    /**
     * Extract complete token metadata
     * ✅ Parse once, get all info!
     */
    public TokenMetadata extractTokenMetadata(String token) {
        try {
            Claims claims = extractAllClaims(token);
            
            return TokenMetadata.builder()
                    .tokenId(token)
                    .userId(claims.get(SecurityConstants.CLAIM_USER_ID, Long.class))
                    .email(claims.getSubject())
                    .role(claims.get(SecurityConstants.CLAIM_ROLE, String.class))
                    .deviceId(claims.get(SecurityConstants.CLAIM_DEVICE_ID, String.class))
                    .tokenType(TokenType.valueOf(claims.get(SecurityConstants.CLAIM_TOKEN_TYPE, String.class)))
                    .issuedAt(claims.getIssuedAt())
                    .expiresAt(claims.getExpiration())
                    .build();
        } catch (TokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error extracting token metadata", e);
            throw new TokenException(ErrorCode.TOKEN_INVALID, "Error parsing token", e);
        }
    }

    /**
     * Validate token (basic check)
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (TokenException e) {
            return false;
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            TokenMetadata metadata = extractTokenMetadata(token);
            return metadata.isExpired();
        } catch (TokenException e) {
            if (e.getErrorCode() == ErrorCode.TOKEN_EXPIRED) {
                return true;
            }
            throw e;
        }
    }

    // =========================
    // QUICK ACCESS METHODS (for backward compatibility)
    // =========================

    public Long getUserIdFromToken(String token) {
        try {
            return extractTokenMetadata(token).getUserId();
        } catch (TokenException e) {
            log.debug("Cannot extract userId from token: {}", e.getMessage());
            return null;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            return extractTokenMetadata(token).getEmail();
        } catch (TokenException e) {
            log.debug("Cannot extract email from token: {}", e.getMessage());
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        try {
            return extractTokenMetadata(token).getRole();
        } catch (TokenException e) {
            log.debug("Cannot extract role from token: {}", e.getMessage());
            return null;
        }
    }

    public String getDeviceIdFromToken(String token) {
        try {
            return extractTokenMetadata(token).getDeviceId();
        } catch (TokenException e) {
            log.debug("Cannot extract deviceId from token: {}", e.getMessage());
            return null;
        }
    }

    public long getRemainingValidity(String token) {
        try {
            return extractTokenMetadata(token).getRemainingValidity();
        } catch (TokenException e) {
            return 0;
        }
    }

    // =========================
    // GETTERS
    // =========================

    public long getAccessTokenExpiration() {
        return SecurityConstants.ACCESS_TOKEN_VALIDITY;
    }

    public long getRefreshTokenExpiration() {
        return SecurityConstants.REFRESH_TOKEN_VALIDITY;
    }
}
