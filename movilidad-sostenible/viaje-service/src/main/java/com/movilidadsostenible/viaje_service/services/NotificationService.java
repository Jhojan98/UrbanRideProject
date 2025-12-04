package com.movilidadsostenible.viaje_service.services;

import com.movilidadsostenible.viaje_service.controllers.SseController;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SseController sseController;

    public NotificationService(SseController sseController) {
        this.sseController = sseController;
    }

    private static final String EVENT_TRAVEL_EXPIRATION = "EXPIRED_TRAVEL";
    private static final String EVENT_TRAVEL_START = "START_TRAVEL";
    private static final String EVENT_TRAVEL_END = "END_TRAVEL";

    public void notificar(String estado, Integer caso) {
        if (caso == 1) {
            sseController.sendToAll(EVENT_TRAVEL_EXPIRATION + ": " + estado);
        } else if (caso == 2) {
            sseController.sendToAll(EVENT_TRAVEL_START + ": " + estado);
        } else if (caso == 3) {
            sseController.sendToAll(EVENT_TRAVEL_END + ": " + estado);
        } else {
            System.out.println("Caso no reconocido para notificación.");
        }
    }
}
