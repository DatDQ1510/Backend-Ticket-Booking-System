package com.example.demo.controller;


import com.example.demo.custom.MailService;
import com.example.demo.custom.OtpService;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/reset-password")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final OtpService otpService;

    @PostMapping("/forgot-password" )
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        // Implementation for sending OTP to user's email
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Generate OTP
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        // Save OTP in Redis
        otpService.saveOtp(email, otp);
        // Send OTP via email
        mailService.sendOtp(email, otp);

        return ResponseEntity.ok("OTP sent to email");
    }

    @PostMapping
    public ResponseEntity<?> resetPassword(@RequestParam String email,
                                           @RequestParam String otp,
                                           @RequestParam String newPassword)
    {
        // Implementation for resetting password using OTP
        String savedOtp = otpService.getOtp(email);
        if (savedOtp == null || !savedOtp.equals(otp)) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete OTP after successful password reset
        otpService.deleteOtp(email);

        return ResponseEntity.ok("Password reset successfully");
    }
}
