package com.movilidadsostenible.email_service.controllers;

import com.movilidadsostenible.email_service.models.dto.UserDTO;
import com.movilidadsostenible.email_service.models.entity.EmailVerification;
import com.movilidadsostenible.email_service.publisher.EmailVerifiedPublisher;
import com.movilidadsostenible.email_service.services.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
public class EmailVerificationController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private EmailVerifiedPublisher publisher;

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(
            @RequestParam("userCc") Integer userCc,
            @RequestParam("otp") String otp) {

        boolean verified = emailVerificationService.verifyOtp(userCc, otp);

        if (!verified) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "verified", false,
                            "message", "El código OTP es inválido o expiró"
                    ));
        }
        else{
            // Publicar el evento de correo verificado
            Optional<EmailVerification> emailVerification = emailVerificationService.byUserCc(userCc);
            EmailVerification emailVerificationTemp = emailVerification.get();

            UserDTO user = new UserDTO();
            user.setUserCc(emailVerificationTemp.getUserCc());
            user.setUserEmail(emailVerificationTemp.getUserEmail());
            user.setVerified(true);
            publisher.sendJsonMessage(user);
        }

        return ResponseEntity.ok(Map.of(
                "verified", true,
                "message", "Correo verificado exitosamente",
                "userCc", userCc
        ));
    }
}
