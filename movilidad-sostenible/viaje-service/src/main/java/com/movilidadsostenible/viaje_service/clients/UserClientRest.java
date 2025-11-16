package com.movilidadsostenible.viaje_service.clients;

import com.movilidadsostenible.viaje_service.models.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "usuario-service", url = "${service.usuario.url}")
public interface UserClientRest {

    @GetMapping("/{id}")
    User userDetail(@PathVariable Integer id);


}
