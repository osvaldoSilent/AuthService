package com.osvaldevops.auth_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.osvaldevops.auth_service.model.dto.otp.UserRegisteredEvent;


@Service
public class AuthEventPublisher {
    

    private static final Logger log = LoggerFactory.getLogger(AuthEventPublisher.class);
    private static final String TOPIC = "auth.user.registered";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    

    public AuthEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOtpEvent(String userId, String email) {
        // 2. Generar OTP seguro
        String otp = generateSecureOtp(); 
        UserRegisteredEvent event = new UserRegisteredEvent(userId, email, otp);
        
        // userId as partition key
        kafkaTemplate.send(TOPIC, userId, event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Evento publicado con éxito en Kafka. Topic: {}, Partition: {}", 
                             TOPIC, result.getRecordMetadata().partition());
                } else {
                    log.error("Fallo al publicar el evento en Kafka para el usuario: {}", userId, ex);
                }
            });
    }

    public String generateSecureOtp() {
        return String.valueOf(100000 + new java.security.SecureRandom().nextInt(900000));
    }
}
