package com.osvaldevops.auth_service.model.otp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserRegisteredEvent(
    @JsonProperty("user_name")
    String userName,
    String email,
    String otpCode
) {
}
