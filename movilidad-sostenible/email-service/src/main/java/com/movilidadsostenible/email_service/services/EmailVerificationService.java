package com.movilidadsostenible.email_service.services;

import com.movilidadsostenible.email_service.models.dto.UserDTO;
import com.movilidadsostenible.email_service.models.entity.EmailVerification;

import java.util.Optional;

public interface EmailVerificationService {
    Optional<EmailVerification> byId(Integer idEmailVerification);
    Optional<EmailVerification> byUserCc(Integer userCc);
    EmailVerification save(EmailVerification emailVerification);
    void delete(Integer idEmailVerification);

    void processEmailVerification(UserDTO user);
    boolean verifyOtp(Integer userCc, String otp);
    EmailVerification resendOtp(Integer userCc);
}
