package com.movilidadsostenible.payment_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSessionRequest {
    // ID del Price pre-creado en Stripe (por ejemplo price_12345). Obligatorio.
    private String priceId;
    // Cantidad de items (default 1 si no se envía)
    private Integer quantity;
    // Correo del cliente (opcional) para precargar en la sesión
    private String customerEmail;
    // UID del usuario interno de tu sistema (obligatorio ahora)
    private String userId;
}
