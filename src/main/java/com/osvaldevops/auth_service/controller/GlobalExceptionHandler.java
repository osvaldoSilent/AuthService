package com.osvaldevops.auth_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.osvaldevops.auth_service.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        Map<String, Object> body = Map.of(
            "timestamp", LocalDateTime.now(),
            "status", ex.getStatus().value(),
            "error", ex.getStatus().getReasonPhrase(),
            "message", "Message from global handler"
        );

        return new ResponseEntity<>(body, ex.getStatus());
    }
}
