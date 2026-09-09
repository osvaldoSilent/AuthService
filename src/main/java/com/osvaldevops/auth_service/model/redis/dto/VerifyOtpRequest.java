package com.osvaldevops.auth_service.model.redis.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyOtpRequest(
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    String email,

    @NotBlank(message = "El código OTP es obligatorio")
    @Size(min = 6, max = 6, message = "El código OTP debe tener exactamente 6 dígitos")
    String otpCode
) {}