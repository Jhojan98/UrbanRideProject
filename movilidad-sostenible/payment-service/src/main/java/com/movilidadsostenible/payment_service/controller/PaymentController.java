package com.movilidadsostenible.payment_service.controller;

import com.movilidadsostenible.payment_service.model.dto.CheckoutSessionRequest;
import com.movilidadsostenible.payment_service.model.dto.CheckoutSessionResponse;
import com.movilidadsostenible.payment_service.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
//@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
@RestController
@RequestMapping("/payments")
//@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    // ELIMINA esta línea: private final RestTemplate restTemplate;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    @Value("${usuario.service.url:http://localhost:8001}")
    private String usuarioServiceUrl;

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
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            String type = event.getType();
            log.info("Evento Stripe verificado: {}", type);

            switch (type) {
                case "checkout.session.completed" -> {
                    StripeObject stripeObject = event.getData().getObject();
                    if (stripeObject instanceof Session) {
                        Session session = (Session) stripeObject;
                        handleSuccessfulPayment(session);
                    }
                }
                case "payment_intent.succeeded" -> log.info("Pago exitoso: {}", event.getData());
                case "payment_intent.payment_failed" -> log.warn("Pago fallido: {}", event.getData());
                default -> log.info("Evento no manejado: {}", type);
            }

            return ResponseEntity.ok(Map.of("verified", true, "type", type));
        } catch (SignatureVerificationException e) {
            log.error("Firma inválida en webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Firma inválida", "details", e.getMessage()));
        } catch (Exception e) {
            log.error("Error procesando webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno"));
        }
    }

    private void handleSuccessfulPayment(Session session) {
        try {
            log.info("Procesando pago exitoso para sesión: {}", session.getId());

            // Verificar que el pago fue exitoso
            if (!"paid".equals(session.getPaymentStatus())) {
                log.warn("Pago no exitoso para sesión: {}, estado: {}", session.getId(), session.getPaymentStatus());
                return;
            }

            // Obtener metadatos de la sesión
            Map<String, String> metadata = session.getMetadata();
            if (metadata == null) {
                log.error("Metadata no encontrada en la sesión");
                return;
            }

            String userId = metadata.get("userId");
            if (userId == null || userId.isEmpty()) {
                log.error("UserId no encontrado en metadata de la sesión");
                return;
            }

            // Obtener el monto total (en centavos de Stripe)
            Long amountTotal = session.getAmountTotal();
            if (amountTotal == null) {
                log.error("Monto total no encontrado en la sesión");
                return;
            }

            // Convertir de centavos a COP (Stripe usa centavos)
            int amountInCOP = (int) (amountTotal / 100);

            log.info("Actualizando saldo para usuario: {} con monto: {} COP", userId, amountInCOP);

            // Llamar al usuario-service para actualizar el saldo
            updateUserBalance(userId, amountInCOP);

        } catch (Exception e) {
            log.error("Error procesando pago exitoso: {}", e.getMessage(), e);
        }
    }

    private void updateUserBalance(String userId, int amount) {
        try {
            String url = usuarioServiceUrl + "/balance/" + userId + "/add";

            // Crear parámetros de consulta
            String fullUrl = url + "?amount=" + amount;

            log.info("Llamando a usuario-service: {}", fullUrl);

            // CREAR RestTemplate directamente aquí (sin inyección)
            RestTemplate restTemplate = new RestTemplate();

            // Hacer la llamada al usuario-service
            ResponseEntity<Map> response = restTemplate.postForEntity(
                fullUrl,
                null,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                log.info("Saldo actualizado exitosamente: {}", responseBody);
            } else {
                log.error("Error al actualizar saldo. Código: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error al actualizar saldo en usuario-service: {}", e.getMessage(), e);
        }
    }

    private String readRawBody(HttpServletRequest request) throws IOException {
        try (InputStream is = request.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}
