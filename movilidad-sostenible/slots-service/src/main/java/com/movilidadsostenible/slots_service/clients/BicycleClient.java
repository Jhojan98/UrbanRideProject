package com.movilidadsostenible.slots_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "bicis-service")
public interface BicycleClient {

    // Metodo para actualizar el padlockeStatus de una bicicleta en bicis-service
    @PutMapping(path = "/{id}/padlock-status")
    Object updatePadlockStatus(
            @PathVariable("id") String id,
            @RequestParam("status") String status
    );
}
