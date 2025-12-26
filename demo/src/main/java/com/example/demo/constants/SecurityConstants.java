package com.example.demo.constants;

/**
 * Security and JWT related constants
 * Centralized constants to avoid magic strings
 */
public final class SecurityConstants {

    private SecurityConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // JWT Claims
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_DEVICE_ID = "deviceId";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";

    // Token Settings
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String COOKIE_REFRESH_TOKEN = "refreshToken";

    // Redis Key Prefixes (optimized - shorter keys)
    public static final String REDIS_REFRESH_TOKEN_PREFIX = "rt:"; // refresh token
    public static final String REDIS_BLACKLIST_PREFIX = "bl:"; // blacklist

    // Token Expiration (milliseconds)
    public static final long ACCESS_TOKEN_VALIDITY = 50 * 60 * 1000L; // 15 minutes
    public static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000L; // 7 days

    // Security
    public static final int MAX_DEVICES_PER_USER = 5; // Limit concurrent sessions
}
