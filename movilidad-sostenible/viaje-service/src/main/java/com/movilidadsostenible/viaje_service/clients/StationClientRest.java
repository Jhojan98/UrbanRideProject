package com.movilidadsostenible.viaje_service.clients;

import com.movilidadsostenible.viaje_service.models.Bicycle;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "estaciones-service")
public interface StationClientRest {

    @GetMapping("/{id}")
    Bicycle bicycleDetail(@PathVariable Integer id);
}
