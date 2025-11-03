package com.movilidadsostenible.viaje_service.clients;

import com.movilidadsostenible.viaje_service.models.Usuario;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "usuario-service", url = "usuario-service:8001")
public interface UsuarioClientRest {

    @GetMapping("/{id}")
    Usuario detalleUsuario(@PathVariable Integer id);


}
