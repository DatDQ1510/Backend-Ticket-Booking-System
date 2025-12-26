package com.example.demo.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response DTO
 * Refactored with Builder pattern and expiresIn field
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String email;
    private String role;
    private String accessToken;
    private Long expiresIn; // Access token expiration in milliseconds
}
