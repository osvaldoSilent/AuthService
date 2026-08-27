package com.osvaldevops.auth_service.controller;

import com.osvaldevops.auth_service.model.dto.auth.AuthResponse;
import com.osvaldevops.auth_service.model.dto.auth.LoginRequest;
import com.osvaldevops.auth_service.model.dto.auth.RegisterRequest;
import com.osvaldevops.auth_service.service.AuthService;
import com.osvaldevops.auth_service.service.AuthEventPublisher;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthEventPublisher authEventPublisher;

    public AuthController(AuthService authService, AuthEventPublisher authEventPublisher) {
        this.authEventPublisher = authEventPublisher;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        authEventPublisher.publishOtpEvent(request.user_name(), request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}