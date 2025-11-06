package com.movilidadsostenible.viaje_service.clients;

import com.movilidadsostenible.viaje_service.models.Bicicleta;
import com.movilidadsostenible.viaje_service.models.Usuario;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "bicis-service", url = "${service.bicis.url}")
public interface BicicletaClientRest {

    @GetMapping("/{id}")
    Bicicleta detalleBicicleta(@PathVariable Integer id);


}
