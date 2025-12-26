package com.example.demo.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenRedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Thêm token vào Redis với TTL
    public void saveToken(String key, String token, long ttlMinutes) {
        redisTemplate.opsForValue().set(key, token, ttlMinutes, TimeUnit.MINUTES);
    }

    // Kiểm tra token có tồn tại trong Redis không
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // Xóa token khỏi Redis
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
