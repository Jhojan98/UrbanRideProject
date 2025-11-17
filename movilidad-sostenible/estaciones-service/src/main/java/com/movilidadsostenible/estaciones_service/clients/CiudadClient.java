package com.movilidadsostenible.estaciones_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ciudad-service")
public interface CiudadClient {

    @GetMapping("/cities/{id}")
    ResponseEntity<?> getCityById(@PathVariable("id") Integer id);
}

