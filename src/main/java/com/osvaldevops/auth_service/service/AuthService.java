package com.osvaldevops.auth_service.service;
 

import com.osvaldevops.auth_service.config.security.JwtService;
import com.osvaldevops.auth_service.exception.BusinessException;
import com.osvaldevops.auth_service.model.UserEntity;
import com.osvaldevops.auth_service.model.auth.dto.AuthResponse;
import com.osvaldevops.auth_service.model.auth.dto.LoginRequest;
import com.osvaldevops.auth_service.model.auth.dto.RegisterRequest;
import com.osvaldevops.auth_service.model.auth.enums.UserRole;
import com.osvaldevops.auth_service.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final long accessTokenExpirationMs;
    private final EmailValidationService emailValidationService;
    private final AuthEventPublisher authEventPublisher;


    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            EmailValidationService emailValidationService,
            AuthEventPublisher authEventPublisher,
            @Value("${security.jwt.access-token-expiration-ms}") long accessTokenExpirationMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.emailValidationService = emailValidationService;
        this.authEventPublisher = authEventPublisher;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }
        boolean isEmailValid = emailValidationService.isEmailRealAndValid(request.email());
        if (!isEmailValid) {
            throw new BusinessException(
                "El correo proporcionado no es un correo válido o su dominio no puede recibir mensajes", 
                HttpStatus.BAD_REQUEST
            );
        }
        var user = UserEntity.builder()
                .user_name(request.user_name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.ROLE_USER)
                .enabled(false)
                .build();

        userRepository.save(user);
        var accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        var refreshToken = jwtService.generateRefreshToken(user.getEmail());
        try{
            authEventPublisher.publishOtpEvent(request.user_name(), request.email());
        }
        catch(Exception e){
            
        }
        return AuthResponse.of(accessToken, refreshToken, accessTokenExpirationMs);
    }

    public AuthResponse login(LoginRequest request) {
        
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier(), request.password())
        );
        
        var user = userRepository.findByUsernameOrEmail(request.identifier(),request.identifier())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        var accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        var refreshToken = jwtService.generateRefreshToken(user.getEmail());
        return AuthResponse.of(accessToken, refreshToken, accessTokenExpirationMs);
    }

    
}