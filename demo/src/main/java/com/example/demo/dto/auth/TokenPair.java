package com.example.demo.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token pair containing access token and refresh token
 * Used when generating new tokens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenPair {
    
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn; // milliseconds
    private Long refreshTokenExpiresIn; // milliseconds
}
