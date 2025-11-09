package com.movilidadsostenible.email_service.services;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class OtpService {

    public String generateOtp() {
        return String.valueOf(new Random().nextInt(999999));
    }

    public String hashOtp(String otp) {
        return BCrypt.hashpw(otp, BCrypt.gensalt());
    }

    public boolean verifyOtp(String raw, String hashed) {
        return BCrypt.checkpw(raw, hashed);
    }
}
