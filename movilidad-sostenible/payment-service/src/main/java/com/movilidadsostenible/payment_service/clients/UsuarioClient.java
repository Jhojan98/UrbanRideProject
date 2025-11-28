package com.movilidadsostenible.payment_service.clients;

import com.movilidadsostenible.payment_service.model.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "usuario-service")
public interface UsuarioClient {

    // Endpoint del usuario-service: POST /balance/{uid}/add?amount=NN
    @PostMapping("/balance/{uid}/add")
    ResponseEntity<?> addBalance(@PathVariable("uid") String uid,
                                 @RequestParam("amount") Integer amount);

    // Versión con JSON body (preferida)
    @PostMapping(value = "/balance/{uid}/add", consumes = "application/json")
    ResponseEntity<?> addBalanceDto(@PathVariable("uid") String uid,
                                    @RequestBody UserDTO body);
}
