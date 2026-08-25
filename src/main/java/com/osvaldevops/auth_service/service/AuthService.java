package com.osvaldevops.auth_service.service;
 

import com.osvaldevops.auth_service.model.enums.UserRole;
import com.osvaldevops.auth_service.exception.BusinessException;
import com.osvaldevops.auth_service.model.UserEntity;
import com.osvaldevops.auth_service.model.dto.auth.AuthResponse;
import com.osvaldevops.auth_service.model.dto.auth.LoginRequest;
import com.osvaldevops.auth_service.model.dto.auth.RegisterRequest;
import com.osvaldevops.auth_service.repository.UserRepository;
import com.osvaldevops.auth_service.config.JwtService;
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
        this.authEventPublisher =authEventPublisher;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        System.out.println("entra");
        boolean isEmailValid = emailValidationService.isEmailRealAndValid(request.email());
        //if (!isEmailValid) {
        //    throw new BusinessException(
        //        "El correo proporcionado no es un correo válido o su dominio no puede recibir mensajes", 
        //        HttpStatus.BAD_REQUEST
        //    );
        //}
        System.out.println("entra22");
        
        // 1. Validar que el email no exista
        //if (userRepository.existsByEmail(request.email())) {
            // Lanza excepción que Spring Boot traducirá a RFC 7807 (Problem Details)
            //throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        //}
        System.out.println("entra 2");
        var user = UserEntity.builder()
                .user_name(request.user_name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.ROLE_USER)
                .enabled(false)
                .build();

        userRepository.save(user);
        System.out.println("entra 3");
        var accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        var refreshToken = jwtService.generateRefreshToken(user.getEmail());
        System.out.println("entra 4");
        // 2. Generar OTP seguro
        String otp = generateSecureOtp(); 

        // 3. Publicar evento a Kafka (El Notification Worker lo consumirá)
        System.out.println("enviando evento");
        authEventPublisher.publishOtpEvent(user.getId().toString(), user.getEmail(), otp);

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
        System.out.println("PASA 3");
        return AuthResponse.of(accessToken, refreshToken, accessTokenExpirationMs);
    }

    private String generateSecureOtp() {
        return String.valueOf(100000 + new java.security.SecureRandom().nextInt(900000));
    }
}