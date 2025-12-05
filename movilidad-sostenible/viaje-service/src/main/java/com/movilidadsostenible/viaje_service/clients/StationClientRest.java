package com.movilidadsostenible.viaje_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;

@FeignClient(name = "estaciones-service")
public interface StationClientRest {

    // obtiene solo el campo `type` de la estación
    @GetMapping("/{id}/type")
    ResponseEntity<String> getTypeById(@PathVariable Integer id);

}
