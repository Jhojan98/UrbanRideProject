package com.movilidadsostenible.email_service.controllers;

import com.movilidadsostenible.email_service.models.dto.UserDTO;
import com.movilidadsostenible.email_service.models.entity.EmailVerification;
import com.movilidadsostenible.email_service.publisher.EmailVerifiedPublisher;
import com.movilidadsostenible.email_service.services.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@Tag(name = "Verificación de Email", description = "Endpoints para verificar correos electrónicos mediante OTP")
public class EmailVerificationController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private EmailVerifiedPublisher publisher;

    @GetMapping("/verify")
    @Operation(summary = "Verificar correo electrónico",
            description = "Verifica el correo de un usuario usando un código OTP válido y no expirado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Correo verificado",
                            content = @Content(schema = @Schema(implementation = Map.class))),
                    @ApiResponse(responseCode = "400", description = "OTP inválido o expirado",
                            content = @Content(schema = @Schema(implementation = Map.class)))
            })
    public ResponseEntity<?> verifyEmail(
            @Parameter(description = "Documento del usuario", required = true)
            @RequestParam("userCc") Integer userCc,
            @Parameter(description = "Código OTP enviado al correo", required = true)
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
            user.setUserEmail("");
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
