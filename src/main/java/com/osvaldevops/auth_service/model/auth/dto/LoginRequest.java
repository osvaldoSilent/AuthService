package com.osvaldevops.auth_service.model.auth.dto;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Email or Username is required")
    String identifier,
    @NotBlank(message = "password is mandatory")
    String password
) {}