package com.example.demo.component;

import com.example.demo.custom.CustomUserDetails;
import com.example.demo.dto.auth.TokenPair;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.auth.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // Lấy user từ database
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found after OAuth2 authentication"));

        Long userId = user.getUserId();
        String role = user.getRole().name();

        // 🔹 Tạo CustomUserDetails (hoặc lớp UserDetails của bạn)
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // 🔹 Tạo Authentication mới để ghi vào SecurityContext
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 🔹 Sinh token (JWT) như trước
        TokenPair tokenPair = tokenService.generateTokenPair(userId, email, role);

        log.info("OAuth2 login successful for user: {} (userId: {})", email, userId);

        // 🔹 Redirect về frontend kèm token
        String redirectUrl = buildRedirectUrl(tokenPair);
        response.sendRedirect(redirectUrl);
    }

    private String buildRedirectUrl(TokenPair tokenPair) {
        return "http://localhost:5173/oauth2/redirect"
                + "?accessToken=" + tokenPair.getAccessToken()
                + "&refreshToken=" + tokenPair.getRefreshToken();
    }
}
