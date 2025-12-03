package com.movilidadsostenible.viaje_service.controllers;

import com.movilidadsostenible.viaje_service.models.dto.StartTravelRequestDTO;
import com.movilidadsostenible.viaje_service.models.entity.Travel;
import com.movilidadsostenible.viaje_service.models.dto.ReservationTempDTO;
import com.movilidadsostenible.viaje_service.services.TravelService;
import com.movilidadsostenible.viaje_service.services.ReservationTempService;
import com.movilidadsostenible.viaje_service.clients.SlotsClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.UUID;

@RestController
@Tag(name = "Viajes", description = "Operaciones para gestionar viajes")
public class TravelController {
    @Autowired
    private TravelService service;

    @Autowired
    private SlotsClient slotsClient;

    @Autowired
    private ReservationTempService reservationTempService;

    @GetMapping
    @Operation(summary = "Listar viajes")
    public List<Travel> listTravels() {
        return service.listTravels();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener viaje por id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Encontrado",
                            content = @Content(schema = @Schema(implementation = Travel.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrado")
            })
    public ResponseEntity<?> getTravelsById(
            @Parameter(description = "Identificador del viaje", required = true)
            @PathVariable Integer id) {
        Optional<Travel> usuarioOptional = service.byId(id);

        if(usuarioOptional.isPresent()) {
            return ResponseEntity.ok(usuarioOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/viaje/usuario/{id}")
    @Operation(summary = "Obtener viaje por id de usuario")
    public ResponseEntity<?> obtenerViajesPorIdUsuario(
            @Parameter(description = "Identificador del usuario", required = true)
            @PathVariable Integer id) {
        Optional<Travel> usuarioOptional = service.byId(id);

        if(usuarioOptional.isPresent()) {
            return ResponseEntity.ok(usuarioOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/viaje/bicicleta/{id}")
    @Operation(summary = "Obtener viaje por id de bicicleta")
    public ResponseEntity<?> obtenerViajesPorIdBicicleta(
            @Parameter(description = "Identificador de la bicicleta", required = true)
            @PathVariable Integer id) {
        Optional<Travel> usuarioOptional = service.byId(id);

        if(usuarioOptional.isPresent()) {
            return ResponseEntity.ok(usuarioOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear viaje",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Creado",
                            content = @Content(schema = @Schema(implementation = Travel.class))),
                    @ApiResponse(responseCode = "400", description = "Validación fallida")
            })
    public ResponseEntity<?> createTravel(@Valid @RequestBody Travel travel,
                                          BindingResult result) {

        if (result.hasErrors()) {
            return validate(result);
        }
        try{
            Optional<Travel> viajeGuardado = Optional.ofNullable(service.save(travel));
            if (viajeGuardado.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("mensaje", "No se pudo crear el travel. Verifique que el usuario y la bicicleta existan."));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(viajeGuardado.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("mensaje", "Error al crear el travel: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar viaje")
    public ResponseEntity<?> deleteTravel(@PathVariable Integer id) {
        Optional<Travel> userOptional = service.byId(id);
        if (userOptional.isPresent()) {
            service.delete(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/start")
    @Operation(summary = "Iniciar viaje temporal",
            description = "Recibe userUid, stationStartId, stationEndId y bikeType (ELECTRIC o MECHANIC). Reserva un slot adecuado mediante SlotsClient, guarda la reserva temporal en Redis (no persiste), publica un mensaje delay en Rabbit para expiración TTL y devuelve reservationId y slotId.")
    public ResponseEntity<?> startTravel(
            @RequestBody StartTravelRequestDTO req
    ) {
        // Validaciones básicas
        if (req == null || req.getUserUid() == null || req.getStationStartId() == null || req.getStationEndId() == null || req.getBikeType() == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("mensaje", "userUid, stationStartId, stationEndId y bikeType son requeridos"));
        }

        String bikeType = req.getBikeType().trim().toUpperCase(Locale.ROOT);
        ResponseEntity<String> slotResp;
        ResponseEntity<String> slotEndId;
        try {
            if ("ELECTRIC".equals(bikeType)) {
                slotResp = slotsClient.reserveFirstAvailableElectric(req.getStationStartId());
            } else if ("MECHANIC".equals(bikeType)) {
                slotResp = slotsClient.reserveFirstAvailableMechanic(req.getStationStartId());
            } else {
                return ResponseEntity.badRequest().body(Collections.singletonMap("mensaje", "bikeType inválido: debe ser ELECTRIC o MECHANIC"));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("mensaje", "Error al solicitar slot: " + ex.getMessage()));
        }
        try {
            slotEndId = slotsClient.reserveFirstUnlocked(req.getStationEndId());

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("mensaje", "Error al solicitar slot: " + ex.getMessage()));
        }

        if (slotResp == null || !slotResp.getStatusCode().is2xxSuccessful() || slotResp.getBody() == null || slotResp.getBody().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("mensaje", "No hay slots disponibles en la estación solicitada"));
        }

        // Separar slotId y bicycleId del cuerpo recibido (formato: "slotId|bicycleId")
        String slotIdRaw = slotResp.getBody();
        String slotId;
        String bicycleIdParsed = null;
        String[] parts = slotIdRaw.split("\\|");
        if (parts.length >= 2) {
            slotId = parts[0];
            bicycleIdParsed = parts[1];
        } else {
            // Si no viene con '|', se asume que solo es el slotId
            slotId = slotIdRaw;
        }

        String slotEndIdBody = slotEndId.getBody();
        String reservationId = UUID.randomUUID().toString();

        // Crear DTO temporal y guardarlo en Redis (bicycleId = 0 por ahora)
        ReservationTempDTO dto = new ReservationTempDTO();
        dto.setReservationId(reservationId);
        dto.setUserId(req.getUserUid());
        dto.setBicycleId(bicycleIdParsed);
        dto.setStationStartId(req.getStationStartId());
        dto.setSlotStartId(slotId);
        dto.setStationEndId(req.getStationEndId());
        dto.setSlotEndId(slotEndIdBody);
        dto.setTravelType(bikeType);
        dto.setCreatedAt(System.currentTimeMillis());

        reservationTempService.save(dto);

        // Publicar mensaje TTL en Rabbit para manejar expiración (incluye bicycleId si está disponible)
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("reservationId", reservationId);
            message.put("slotId", slotId);
            message.put("userUid", req.getUserUid());
            message.put("stationStartId", req.getStationStartId());
            message.put("stationEndId", req.getStationEndId());
            message.put("bikeType", bikeType);
            if (bicycleIdParsed != null) {
                message.put("bicycleId", bicycleIdParsed);
            }
        } catch (Exception ex) {
            // limpiar temporal si falla la publicación
            reservationTempService.remove(reservationId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("mensaje", "Error al publicar mensaje de expiración: " + ex.getMessage()));
        }

        Map<String, String> resp = new HashMap<>();
        resp.put("reservationId", reservationId);
        resp.put("slotId", slotId);
        return ResponseEntity.ok(resp);
    }

    private ResponseEntity<Map<String, String>> validate(BindingResult result) {
        Map<String,String> errors = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
