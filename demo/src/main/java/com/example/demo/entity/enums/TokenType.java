package com.example.demo.entity.enums;

/**
 * Token type enumeration
 * Helps distinguish between different token types in the system
 */
public enum TokenType {
    ACCESS,     // Short-lived token for API access
    REFRESH     // Long-lived token for getting new access tokens
}
