package com.movilidadsostenible.payment_service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movilidadsostenible.payment_service.clients.UsuarioClient;
import com.movilidadsostenible.payment_service.model.dto.CheckoutSessionRequest;
import com.movilidadsostenible.payment_service.model.dto.CheckoutSessionResponse;
import com.movilidadsostenible.payment_service.model.dto.UserDTO;
import com.movilidadsostenible.payment_service.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UsuarioClient usuarioClient;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Set<String> ZERO_DECIMAL_CURRENCIES = Set.of(
            "bif","clp","djf","gnf","jpy","kmf","krw","mga","pyg","rwf","ugx","vnd","vuv","xaf","xof","xpf"
    );

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
    public ResponseEntity<?> handleWebhook(
            HttpServletRequest request,
            @RequestHeader(name = "Stripe-Signature", required = false) String sigHeader
    ) throws IOException {

        String payload = readRawBody(request);

        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Webhook secret no configurado");
            return ResponseEntity.ok(Map.of("received", true, "verified", false));
        }
        if (sigHeader == null || sigHeader.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Falta Stripe-Signature"));
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Firma inválida"));
        }

        String type = event.getType();
        log.info("Webhook Stripe verificado: {}", type);

        // ---- PARSEO SIMPLE ----
        JsonNode obj = MAPPER.readTree(payload).path("data").path("object");

        String uid = null;
        String email = null;
        Long amount = null;
        String currency = obj.path("currency").asText(null);

        if ("checkout.session.completed".equals(type)) {
            amount = obj.path("amount_total").asLong();
            uid = obj.path("client_reference_id").asText(null);
            email = obj.path("customer_details").path("email").asText(null);
        }

        if ("payment_intent.succeeded".equals(type)) {
            amount = obj.path("amount").asLong();
            uid = obj.path("metadata").path("client_reference_id").asText(null);
            email = obj.path("receipt_email").asText(null);
        }

        // ---- OPERACIÓN: RECARGAR SALDO ----
        if (uid != null && amount != null) {
            int amountInteger = (currency != null && !ZERO_DECIMAL_CURRENCIES.contains(currency))
                    ? (int) (amount / 100)
                    : amount.intValue();

            usuarioClient.addBalance(uid, amountInteger);

            log.info("Saldo añadido -> uid={}, amount={}, email={}", uid, amountInteger, email);
        }

        return ResponseEntity.ok(Map.of(
                "verified", true,
                "type", type,
                "uid", uid,
                "email", email,
                "amountMinor", amount,
                "currency", currency
        ));
    }

    private String readRawBody(HttpServletRequest request) throws IOException {
        return new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
