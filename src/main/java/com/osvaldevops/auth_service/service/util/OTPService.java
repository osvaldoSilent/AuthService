package com.osvaldevops.auth_service.service.util;

import org.springframework.stereotype.Service;

@Service 
public class OTPService {
    public String generateSecureOtp() {
        return String.valueOf(100000 + new java.security.SecureRandom().nextInt(900000));
    }

    

}
