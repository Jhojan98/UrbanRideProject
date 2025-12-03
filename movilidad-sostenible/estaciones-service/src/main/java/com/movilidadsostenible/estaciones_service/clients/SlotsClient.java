package com.movilidadsostenible.estaciones_service.clients;

import com.movilidadsostenible.estaciones_service.model.dto.SlotRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "slots-service")
public interface SlotsClient {
    @PostMapping("/create")
    ResponseEntity<?> createSlot(@RequestBody SlotRequestDTO slot);
}

