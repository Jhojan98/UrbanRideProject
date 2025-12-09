package com.movilidadsostenible.viaje_service.clients;

import com.movilidadsostenible.viaje_service.models.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "usuario-service")
public interface UserClientRest {

    // Cobro de viaje en usuario-service
    @PostMapping("/travel/charge/{uid}")
    void chargeTrip(
            @PathVariable("uid") String uidUser,
            @RequestParam("total") Double totalTripValue,
            @RequestParam("excessMinutes") Integer excessMinutes
    );

    // Consulta si el usuario está bloqueado para viajar (true si NO puede viajar, false si SÍ puede)
    @GetMapping("/travel/blocked/{uid}")
    Map<String, Object> isBlockedForTravel(
            @PathVariable("uid") String uidUser
    );

}
