package com.osvaldevops.auth_service.service.redis;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OtpRedisService {

    private final StringRedisTemplate redisTemplate;
    private static final String OTP_PREFIX = "otp:";
    private static final int OTP_TTL_MINUTES = 5;

    public OtpRedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveOtp(String email, String otp) {
        redisTemplate.opsForValue().set(
            OTP_PREFIX + email, 
            otp, 
            Duration.ofMinutes(OTP_TTL_MINUTES)
        );
    }

    public String getOtp(String email) {
        return redisTemplate.opsForValue().get(OTP_PREFIX + email);
    }
    public void deleteOtp(String email) {
        redisTemplate.delete(OTP_PREFIX + email);
    }
}
