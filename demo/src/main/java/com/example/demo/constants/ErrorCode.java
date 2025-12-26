package com.example.demo.constants;

import lombok.Getter;

/**
 * Standardized error codes for the application
 * Makes error handling consistent and internationalization easier
 */
@Getter
public enum ErrorCode {

    // Authentication Errors (1000-1099)
    INVALID_CREDENTIALS("AUTH_1001", "Invalid email or password"),
    TOKEN_EXPIRED("AUTH_1002", "Token has expired"),
    TOKEN_INVALID("AUTH_1003", "Invalid token"),
    REFRESH_TOKEN_NOT_FOUND("AUTH_1004", "Refresh token not found"),
    REFRESH_TOKEN_EXPIRED("AUTH_1005", "Refresh token has expired"),
    UNAUTHORIZED("AUTH_1006", "Unauthorized access"),
    TOKEN_BLACKLISTED("AUTH_1007", "Token has been revoked"),

    // User Errors (2000-2099)
    USER_NOT_FOUND("USER_2001", "User not found"),
    USER_ALREADY_EXISTS("USER_2002", "User already exists"),
    USER_DISABLED("USER_2003", "User account is disabled"),

    // Validation Errors (3000-3099)
    VALIDATION_ERROR("VAL_3001", "Validation error"),
    INVALID_INPUT("VAL_3002", "Invalid input"),

    // Server Errors (5000-5099)
    INTERNAL_SERVER_ERROR("SRV_5001", "Internal server error"),
    SERVICE_UNAVAILABLE("SRV_5002", "Service temporarily unavailable");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
