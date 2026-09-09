package com.osvaldevops.auth_service.service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.osvaldevops.auth_service.model.otp.dto.UserRegisteredEvent;
import com.osvaldevops.auth_service.service.util.Encryption;


@Service
public class AuthEventPublisher {
    

    private static final Logger log = LoggerFactory.getLogger(AuthEventPublisher.class);
    private static final String TOPIC = "auth.user.registered";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Encryption encryption;

    public AuthEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, Encryption encryption) {
        this.kafkaTemplate = kafkaTemplate;
        this.encryption = encryption;
    }

    public void publishOtpEvent(String userId, String email, String otpCode) {
        otpCode = encryption.encrypt(otpCode);
        UserRegisteredEvent event = new UserRegisteredEvent(userId, email, otpCode);
        
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
}
