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

    @PostMapping("/verify/resend")
    @Operation(summary = "Reenviar código de verificación",
            description = "Genera y envía un nuevo código OTP al correo registrado del usuario.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OTP reenviado",
                            content = @Content(schema = @Schema(implementation = Map.class))),
                    @ApiResponse(responseCode = "404", description = "Usuario no tiene verificación pendiente")
            })
    public ResponseEntity<?> resendOtp(
            @Parameter(description = "Documento del usuario", required = true)
            @RequestParam("userCc") Integer userCc) {
        try {
            var nuevo = emailVerificationService.resendOtp(userCc);
            return ResponseEntity.ok(Map.of(
                    "resent", true,
                    "message", "Se envió un nuevo código de verificación al correo",
                    "userCc", userCc,
                    "expiresAt", String.valueOf(nuevo.getExpiresAt())
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "resent", false,
                            "message", ex.getMessage()
                    ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "resent", false,
                            "message", "Error al reenviar OTP: " + ex.getMessage()
                    ));
        }
    }
}
