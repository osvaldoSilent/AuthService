package com.osvaldevops.auth_service.model.dto.otp;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserRegisteredEvent(
    @JsonProperty("user_name")
    String userId,
    String email,
    String otpCode,
    String createdAt    
) {
    public UserRegisteredEvent(String userId, String email, String otp) {
        this(userId, email, otp, Instant.now().toString());
    }
}
