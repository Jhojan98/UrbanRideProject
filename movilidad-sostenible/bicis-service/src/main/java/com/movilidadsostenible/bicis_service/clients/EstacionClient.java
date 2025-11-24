package com.movilidadsostenible.bicis_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "estaciones-service")
public interface EstacionClient {

    @GetMapping("/city/{id}")
    ResponseEntity<?> getCityById(@PathVariable("id") Integer id);
}
