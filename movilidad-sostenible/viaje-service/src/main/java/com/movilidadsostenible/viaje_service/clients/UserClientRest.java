package com.movilidadsostenible.viaje_service.clients;

import com.movilidadsostenible.viaje_service.models.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "usuario-service")
public interface UserClientRest {

    @GetMapping("/{id}")
    User userDetail(@PathVariable Integer id);

    // Cobro de viaje en usuario-service
    @PostMapping("/travel/charge/{uid}")
    void chargeTrip(
            @PathVariable("uid") String uidUser,
            @RequestParam("total") Integer totalTripValue,
            @RequestParam("excessMinutes") Integer excessMinutes
    );

}
