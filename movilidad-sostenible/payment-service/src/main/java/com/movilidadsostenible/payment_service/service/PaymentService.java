package com.movilidadsostenible.payment_service.service;

import com.movilidadsostenible.payment_service.model.dto.CheckoutSessionRequest;
import com.movilidadsostenible.payment_service.model.dto.CheckoutSessionResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${stripe.secret-key}")
    private String secretKey;
    
    @Value("${stripe.checkout.success-url}")
    private String successUrl;
    
    @Value("${stripe.checkout.cancel-url}")
    private String cancelUrl;

    public CheckoutSessionResponse createCheckoutSession(CheckoutSessionRequest request) throws StripeException {
        Stripe.apiKey = secretKey;
        
        // Agregar metadata para identificar al usuario y el monto
        Map<String, String> metadata = new HashMap<>();
        metadata.put("userId", request.getUserId());
        metadata.put("priceId", request.getPriceId());
        if (request.getCustomerEmail() != null) {
            metadata.put("customerEmail", request.getCustomerEmail());
        }
        
        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(request.getPriceId())
                                .setQuantity((long) (request.getQuantity() != null ? request.getQuantity() : 1))
                                .build()
                )
                .setCustomerEmail(request.getCustomerEmail())
                .putAllMetadata(metadata)  // Agregar metadata
                .build();

        Session session = Session.create(params);
        return new CheckoutSessionResponse(session.getId(), session.getUrl());
    }
}