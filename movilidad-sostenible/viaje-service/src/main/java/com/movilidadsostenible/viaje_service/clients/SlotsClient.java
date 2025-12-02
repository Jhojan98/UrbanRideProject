package com.movilidadsostenible.viaje_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "slots-service")
public interface SlotsClient {

    // Consumir endpoint que reserva primer slot LOCKED para bicicleta ELECTRIC
    @PostMapping("/stations/{stationId}/reserve-electric")
    ResponseEntity<String> reserveFirstAvailableElectric(@PathVariable("stationId") Integer stationId);

    // Consumir endpoint que reserva primer slot LOCKED para bicicleta MECHANIC
    @PostMapping("/stations/{stationId}/reserve-mechanic")
    ResponseEntity<String> reserveFirstAvailableMechanic(@PathVariable("stationId") Integer stationId);
}


