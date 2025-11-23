package com.movilidadsostenible.payment_service.service;

import com.movilidadsostenible.payment_service.model.dto.CheckoutSessionRequest;
import com.movilidadsostenible.payment_service.model.dto.CheckoutSessionResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Value("${stripe.checkout.success-url}")
    private String successUrl;
    @Value("${stripe.checkout.cancel-url}")
    private String cancelUrl;

    public CheckoutSessionResponse createCheckoutSession(CheckoutSessionRequest request) throws StripeException {
        if (request.getPriceId() == null || request.getPriceId().isBlank()) {
            throw new IllegalArgumentException("priceId es requerido");
        }
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new IllegalArgumentException("userId es requerido");
        }
        int quantity = (request.getQuantity() == null || request.getQuantity() < 1) ? 1 : request.getQuantity();

        // Construimos builder base
        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                // Incluir userId en la URL de success para poder relacionarlo al regresar al front
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}&uid=" + request.getUserId())
                .setCancelUrl(cancelUrl)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(request.getPriceId())
                        .setQuantity(Long.valueOf(quantity))
                        .build())
                // Asociar el usuario a la sesión (visible en el Dashboard de Stripe)
                .setClientReferenceId(request.getUserId())
                // Guardar en metadata adicional
                .putMetadata("userId", request.getUserId());

        if (request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank()) {
            builder.setCustomerEmail(request.getCustomerEmail());
        }

        Session session = Session.create(builder.build());
        return new CheckoutSessionResponse(session.getId(), session.getUrl());
    }
}
