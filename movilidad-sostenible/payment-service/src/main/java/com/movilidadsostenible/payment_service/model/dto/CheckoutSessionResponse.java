package com.movilidadsostenible.payment_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckoutSessionResponse {
    private String sessionId;
    private String url; // URL a la que redirigir al usuario (Checkout Hosted Page)
}
