package com.movilidadsostenible.payment_service.controller;

import com.movilidadsostenible.payment_service.model.dto.CheckoutSessionRequest;
import com.movilidadsostenible.payment_service.model.dto.CheckoutSessionResponse;
import com.movilidadsostenible.payment_service.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payments") // Restaurado base path para coincidir con docker-compose forwarding
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret; // Puede estar vacío si no se configura

    @PostMapping("/checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody CheckoutSessionRequest request) {
        try {
            CheckoutSessionResponse response = paymentService.createCheckoutSession(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (StripeException e) {
            log.error("Stripe error creando sesión: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error creando sesión de pago"));
        }
    }

    @PostMapping(value = "/webhook", consumes = "application/json")
    public ResponseEntity<?> handleWebhook(HttpServletRequest request,
                                           @RequestHeader(name = "Stripe-Signature", required = false) String sigHeader) throws IOException {
        String payload = readRawBody(request);
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Webhook secret no configurado, se omite verificación de firma");
            log.debug("Payload recibido (no verificado): {}", payload);
            return ResponseEntity.ok(Map.of("received", true, "verified", false));
        }
        if (sigHeader == null || sigHeader.isBlank()) {
            log.error("Cabecera Stripe-Signature ausente");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Falta cabecera Stripe-Signature"));
        }
        try {
            log.debug("Verificando firma. Header len={}, secret prefix={}", sigHeader.length(), webhookSecret.substring(0, Math.min(10, webhookSecret.length())) + "...");
            com.stripe.model.Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            String type = event.getType();
            log.info("Evento Stripe verificado: {}", type);
            switch (type) {
                case "checkout.session.completed" -> log.info("Checkout completado: {}", event.getData());
                case "payment_intent.succeeded" -> log.info("Pago exitoso: {}", event.getData());
                case "payment_intent.payment_failed" -> log.warn("Pago fallido: {}", event.getData());
                default -> log.info("Evento no manejado: {}", type);
            }
            return ResponseEntity.ok(Map.of("verified", true, "type", type));
        } catch (SignatureVerificationException e) {
            log.error("Firma inválida en webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Firma inválida", "details", e.getMessage()));
        }
    }

    private String readRawBody(HttpServletRequest request) throws IOException {
        try (InputStream is = request.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}
