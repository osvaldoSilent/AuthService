package com.osvaldevops.auth_service.service.otp;

public class OTPService {
    public String generateSecureOtp() {
        return String.valueOf(100000 + new java.security.SecureRandom().nextInt(900000));
    }

    

}
