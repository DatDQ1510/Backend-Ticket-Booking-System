package com.example.demo.custom;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate stringRedisTemplate;

    public void saveOtp(String email, String otp) {
        stringRedisTemplate.opsForValue().set("otp" + email, otp, 15, TimeUnit.MINUTES);
    }
    public String getOtp(String email) {
        return stringRedisTemplate.opsForValue().get("otp" + email);
    }
    public void deleteOtp(String email) {
        stringRedisTemplate.delete("otp" + email);
    }
}
