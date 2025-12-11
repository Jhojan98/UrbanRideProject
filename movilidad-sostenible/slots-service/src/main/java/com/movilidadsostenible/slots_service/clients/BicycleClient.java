package com.movilidadsostenible.slots_service.clients;

import com.movilidadsostenible.slots_service.model.dto.BicycleTelemetryEndTravelDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "bicis-service")
public interface BicycleClient {

    // Metodo para actualizar el padlockeStatus de una bicicleta en bicis-service
    @PutMapping(path = "/{id}/padlock-status")
    Object updatePadlockStatus(
            @PathVariable("id") String id,
            @RequestParam("status") String status
    );

    // Cerrar candado si está cerca de la estación (telemetría)
    @PutMapping(path = "/padlock-close-if-near", consumes = "application/json")
    ResponseEntity<Object> closePadlockIfNearTelemetry(@RequestBody BicycleTelemetryEndTravelDTO telemetry);
}
