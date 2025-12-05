package com.movilidadsostenible.usuario.clients;

import jakarta.ws.rs.GET;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "fine-service")
public interface FineClient {

    @GetMapping("/api/user_fines/{user}/has_fines_unpaid")
    Boolean hasUnpaidFines(@PathVariable("user") String user);
}

