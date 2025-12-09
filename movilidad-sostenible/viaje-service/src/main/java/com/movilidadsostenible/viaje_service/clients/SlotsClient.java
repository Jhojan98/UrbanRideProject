package com.movilidadsostenible.viaje_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "slots-service")
public interface SlotsClient {

    // Consumir endpoint que reserva primer slot LOCKED para bicicleta ELECTRIC
    @PostMapping("/stations/{stationId}/reserve-electric")
    ResponseEntity<String> reserveFirstAvailableElectric(@PathVariable("stationId") Integer stationId);

    // Consumir endpoint que reserva primer slot LOCKED para bicicleta MECHANIC
    @PostMapping("/stations/{stationId}/reserve-mechanic")
    ResponseEntity<String> reserveFirstAvailableMechanic(@PathVariable("stationId") Integer stationId);

    // Consumir endpoint que reserva primer slot UNLOCKED para bicicleta MECHANIC
    @PostMapping("/stations/{stationId}/reserve-first-unlocked")
    ResponseEntity<String> reserveFirstUnlocked(@PathVariable("stationId") Integer stationId);

    // Consumir endpoint que bloquea un slot por ID (LOCKED)
    @PutMapping("/{slotId}/lock")
    ResponseEntity<String> lockSlotById(@PathVariable("slotId") String slotId);

    // Consumir endpoint que bloquea un slot por ID (LOCKED) y asigna bicycleId
    @PostMapping("/{slotId}/lock")
    ResponseEntity<String> lockSlotById(@PathVariable("slotId") String slotId, @RequestParam("bicycleId") String bicycleId);

  // Consumir endpoint que bloquea un slot por ID (UNLOCKED) y asigna bicycleId
  @PostMapping("/{slotId}/unlock")
  ResponseEntity<String> unlockSlotById(@PathVariable("slotId") String slotId, @RequestParam("bicycleId") String bicycleId);


  // Consumir endpoint que actualiza el padlockStatus de un slot por ID
    @PutMapping("/{id}/padlock-status")
    ResponseEntity<String> updatePadlockStatus(
            @PathVariable("id") String id,
            @RequestParam("padlockStatus") String padlockStatus
    );



}
