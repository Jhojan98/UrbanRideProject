package com.movilidadsostenible.payment_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import com.stripe.Stripe;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.publishable-key}")
    private String publishableKey;

    @PostConstruct
    public void setup() {
        if (secretKey == null || secretKey.isBlank()) {
            log.error("Stripe secret key no configurada");
        } else {
            Stripe.apiKey = secretKey;
            log.info("Stripe API key inicializada. Publishable key configurada: {}", publishableKey != null && !publishableKey.isBlank());
        }
    }
}
