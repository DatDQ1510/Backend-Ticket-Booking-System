package com.example.demo.auth;

import com.example.demo.constants.SecurityConstants;
import com.example.demo.dto.auth.RefreshTokenData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * RefreshTokenService - Manages refresh tokens in Redis
 * 
 * Strategy:
 * - Only refresh tokens are stored in Redis
 * - Access tokens are stateless (not stored)
 * - When logout/refresh, old access tokens are blacklisted
 * 
 * Key format: rt:{userId}:{deviceId}
 * Value: JSON of RefreshTokenData
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    /**
     * Save refresh token to Redis
     */
    public void saveRefreshToken(Long userId, String deviceId, RefreshTokenData tokenData, long ttlMillis) {
        try {
            String key = buildRefreshTokenKey(userId, deviceId);
            String value = objectMapper.writeValueAsString(tokenData);
            
            redisTemplate.opsForValue().set(key, value, ttlMillis, TimeUnit.MILLISECONDS);
            
            log.debug("Saved refresh token for userId={}, deviceId={}", userId, deviceId);
        } catch (JsonProcessingException e) {
            log.error("Error serializing refresh token data", e);
            throw new RuntimeException("Error saving refresh token", e);
        }
    }

    /**
     * Get refresh token data from Redis
     */
    public Optional<RefreshTokenData> getRefreshToken(Long userId, String deviceId) {
        try {
            String key = buildRefreshTokenKey(userId, deviceId);
            String value = redisTemplate.opsForValue().get(key);
            
            if (value == null) {
                return Optional.empty();
            }
            
            RefreshTokenData tokenData = objectMapper.readValue(value, RefreshTokenData.class);
            return Optional.of(tokenData);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing refresh token data", e);
            return Optional.empty();
        }
    }

    /**
     * Check if refresh token exists
     */
    public boolean existsRefreshToken(Long userId, String deviceId) {
        String key = buildRefreshTokenKey(userId, deviceId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Delete specific refresh token
     */
    public void deleteRefreshToken(Long userId, String deviceId) {
        String key = buildRefreshTokenKey(userId, deviceId);
        if(redisTemplate.hasKey(key)) {
            log.debug("redisTemplate.hasKey(key) returned true" + this.getRefreshToken(userId, deviceId).toString());
            log.debug("value" + redisTemplate.opsForValue().get(key));
            redisTemplate.delete(key);
        } else {
            log.debug("No refresh token found for userId={}, deviceId={}", userId, deviceId);
        }

        log.debug("Deleted refresh token for userId={}, deviceId={}", userId, deviceId);
    }

    /**
     * Delete all refresh tokens for a user (logout from all devices)
     */
    public void deleteAllRefreshTokensForUser(Long userId) {
        String pattern = buildRefreshTokenPattern(userId);
        Set<String> keys = redisTemplate.keys(pattern);
        
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Deleted {} refresh tokens for userId={}", keys.size(), userId);
        }
    }

    /**
     * Get all device IDs for a user
     */
    public Set<String> getAllDeviceIdsForUser(Long userId) {
        String pattern = buildRefreshTokenPattern(userId);
        Set<String> keys = redisTemplate.keys(pattern);
        
        if (keys == null) {
            return Set.of();
        }
        
        // Extract deviceIds from keys
        return keys.stream()
                .map(key -> key.substring(key.lastIndexOf(':') + 1))
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Enforce max devices per user
     * Remove oldest tokens if limit exceeded
     */
    public void enforceMaxDevices(Long userId) {
        Set<String> deviceIds = getAllDeviceIdsForUser(userId);
        
        if (deviceIds.size() >= SecurityConstants.MAX_DEVICES_PER_USER) {
            log.warn("User {} has {} devices, enforcing limit", userId, deviceIds.size());
            
            // Get oldest device and remove it
            // In production, you might want to track creation time and remove oldest
            String oldestDevice = deviceIds.iterator().next();
            deleteRefreshToken(userId, oldestDevice);
        }
    }

    /**
     * Build Redis key for refresh token
     * Format: rt:{userId}:{deviceId}
     */
    private String buildRefreshTokenKey(Long userId, String deviceId) {
        return String.format("%s%d:%s", SecurityConstants.REDIS_REFRESH_TOKEN_PREFIX, userId, deviceId);
    }

    /**
     * Build Redis key pattern for all tokens of a user
     * Format: rt:{userId}:*
     */
    private String buildRefreshTokenPattern(Long userId) {
        return String.format("%s%d:*", SecurityConstants.REDIS_REFRESH_TOKEN_PREFIX, userId);
    }
}
