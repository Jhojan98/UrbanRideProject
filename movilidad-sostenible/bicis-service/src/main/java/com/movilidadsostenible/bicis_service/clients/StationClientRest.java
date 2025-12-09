package com.movilidadsostenible.bicis_service.clients;

import com.movilidadsostenible.bicis_service.model.dto.StationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "estaciones-service")
public interface StationClientRest {

  @GetMapping("/city/{id}")
  ResponseEntity<?> getCityById(@PathVariable("id") Integer id);

  @GetMapping("/{id}")
  ResponseEntity<StationDTO> getById(@PathVariable Integer id);
}
