package com.example.demo.auth;

import com.example.demo.constants.SecurityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * TokenBlacklistService - Manages blacklisted access tokens
 * 
 * Strategy:
 * - When user logs out or refreshes token, old access token is blacklisted
 * - Blacklist entries expire automatically (TTL = remaining token validity)
 * - Filter checks blacklist before allowing request
 * 
 * Key format: bl:{tokenId}
 * Value: "revoked" (simple marker)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    /**
     * Add token to blacklist
     * 
     * @param tokenId unique token identifier
     * @param ttlMillis remaining validity (token will be blacklisted until it would expire anyway)
     */
    public void blacklistToken(String tokenId, long ttlMillis) {
        if (ttlMillis <= 0) {
            log.debug("Token already expired, no need to blacklist: {}", tokenId);
            return;
        }

        String key = buildBlacklistKey(tokenId);
        redisTemplate.opsForValue().set(key, "revoked", ttlMillis);
        
        log.debug("Blacklisted token: {} for {} ms", tokenId, ttlMillis);
    }

    /**
     * Check if token is blacklisted
     */
    public boolean isBlacklisted(String tokenId) {
        String key = buildBlacklistKey(tokenId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }


    /**
     * Build Redis key for blacklist
     * Format: bl:{tokenId}
     */
    private String buildBlacklistKey(String tokenId) {

        return SecurityConstants.REDIS_BLACKLIST_PREFIX + tokenId;
    }
}
