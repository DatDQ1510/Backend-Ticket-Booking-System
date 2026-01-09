package com.example.demo.config;

import com.example.demo.constants.SecurityConstants;
import com.example.demo.dto.auth.TokenMetadata;
import com.example.demo.exception.TokenException;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.custom.CustomUserDetailsService;
import com.example.demo.auth.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ✅ JwtAuthenticationFilter
 * - Kiểm tra JWT trong mọi request (trừ các endpoint public)
 * - Nếu access token hết hạn, tự động dùng refresh token để tạo token mới
 * - Cho phép request hiện tại đi qua luôn sau khi refresh
 * - Token mới được trả về qua header: X-New-Token
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService blacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                      HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 🔓 Bỏ qua các endpoint public
        if (path.startsWith("/api/auth/")
                || path.equals("/api/auth")
                || path.startsWith("/api/reset-password")
                || path.startsWith("/oauth2")
                || path.startsWith("/api/payment/")
                || path.startsWith("/api/momo/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = extractJwtFromRequest(request);
            
            // 🔍 DEBUG
            log.info("🔍 JWT Filter - Path: {}, Token present: {}", path, jwt != null);

            if (jwt != null) {
                try {
                    // ✅ Validate token (sẽ throw ExpiredJwtException nếu hết hạn)
                    tokenProvider.validateToken(jwt);

                    // 🔹 Token hợp lệ → xác thực người dùng
                    TokenMetadata metadata = tokenProvider.extractTokenMetadata(jwt);
                    log.info("✅ Token valid for {} ", metadata);
                    log.info("🔍 Token metadata - Email: {}, TokenId: {}, Type: {}", 
                        metadata.getEmail(), metadata.getTokenId(), metadata.getTokenType());
                    
                    // Skip blacklist check if tokenId is null (old tokens without jti claim)
                    boolean isBlacklisted = metadata.getTokenId() != null && blacklistService.isBlacklisted(metadata.getTokenId());
                    
                    if (!isBlacklisted) {
                        setAuthentication(metadata.getEmail(), request); // có khả năng gây ra vấn đề hiệu năng vì lần nào cũng phải gọi vào DB check
                        log.info("✅ Authenticated successfully for {} on {}", metadata.getEmail(), path);
                    } else {
                        log.warn("❌ Token is blacklisted - TokenId: {}, Email: {}", 
                            metadata.getTokenId(), metadata.getEmail());
                    }

                } catch (ExpiredJwtException ex) {
                    // ⚠️ Access token hết hạn → thử dùng refresh token
                    log.warn("⏰ Access token expired, attempting refresh...");
                    if (handleExpiredTokenAndRefresh(request, response)) {
                        // ✅ Sau khi refresh thành công, tiếp tục request này luôn
                        filterChain.doFilter(request, response);
                        return;
                    } else {
                        log.error("❌ Refresh failed - returning 401");
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        return;
                    }
                }
            } else {
                log.info("⚠️ No JWT token found in request to {}", path);
            }

        } catch (TokenException e) {
            log.debug("Token authentication failed: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in JWT authentication filter", e);
        }

        // Tiếp tục filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * ✅ Xử lý khi access token hết hạn:
     * - Kiểm tra refresh token
     * - Nếu hợp lệ → tạo token mới, set vào header, set Authentication
     * - Trả true nếu thành công, để request hiện tại được đi tiếp
     */
    private boolean handleExpiredTokenAndRefresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null) {
            log.warn("Access token expired but no refresh token provided");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        try {
            if (tokenProvider.validateToken(refreshToken)) {
                TokenMetadata metadata = tokenProvider.extractTokenMetadata(refreshToken);

                // Skip blacklist check if tokenId is null
                boolean isBlacklisted = metadata.getTokenId() != null && blacklistService.isBlacklisted(metadata.getTokenId());
                
                if (!isBlacklisted) {
                    // 🔹 Tạo access token mới
                    String newAccessToken = tokenProvider.generateAccessToken(
                            metadata.getUserId(),
                            metadata.getEmail(),
                            metadata.getRole(),
                            metadata.getDeviceId()
                    );

                    // ✅ Gửi token mới cho frontend
                    response.setHeader("X-New-Token", newAccessToken);

                    // ✅ Thiết lập lại Authentication để cho phép request đi qua
                    setAuthentication(metadata.getEmail(), request);

                    log.info("✅ Auto-refreshed token for {}", metadata.getEmail());
                    return true;
                } else {
                    log.warn("Refresh token is blacklisted for {}", metadata.getEmail());
                }
            } else {
                log.warn("Invalid or expired refresh token");
            }
        } catch (Exception e) {
            log.error("Failed to refresh token automatically: {}", e.getMessage());
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return false;
    }

    /**
     * ✅ Thiết lập Authentication cho SecurityContextHolder
     */
    private void setAuthentication(String email, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * ✅ Lấy access token từ header Authorization
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(SecurityConstants.HEADER_AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(SecurityConstants.TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * ✅ Lấy refresh token (ưu tiên từ header, sau đó đến cookie)
     */
    private String extractRefreshToken(HttpServletRequest request) {
        String headerToken = request.getHeader("X-Refresh-Token");
        if (StringUtils.hasText(headerToken)) {
            log.debug("Found refresh token in header");
            return headerToken;
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    log.debug("Found refresh token in cookie");
                    return cookie.getValue();
                }
            }
        }

        log.debug("No refresh token found");
        return null;
    }
}
