package com.movilidadsostenible.viaje_service.controllers;

import com.movilidadsostenible.viaje_service.clients.StationClientRest;
import com.movilidadsostenible.viaje_service.clients.UserClientRest;
import com.movilidadsostenible.viaje_service.models.dto.StartTravelRequestDTO;
import com.movilidadsostenible.viaje_service.models.dto.TravelReservationDTO;
import com.movilidadsostenible.viaje_service.models.dto.TravelStartDTO;
import com.movilidadsostenible.viaje_service.models.entity.Travel;
import com.movilidadsostenible.viaje_service.models.dto.ReservationTempDTO;
import com.movilidadsostenible.viaje_service.rabbit.publisher.TravelPublisher;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@Tag(name = "Viajes", description = "Operaciones para gestionar viajes")
public class TravelController {
    @Autowired
    private TravelService service;

    @Autowired
    private SlotsClient slotsClient;

    @Autowired
    private StationClientRest stationClient;

    @Autowired
    private ReservationTempService reservationTempService;

    @Autowired
    private UserClientRest userClientRest;

    @Autowired
    private TravelPublisher travelPublisher;

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

    // Nuevo endpoint: Obtener todos los viajes de un usuario a partir de su UID (identificador único del usuario)
    @GetMapping("/usuario/{uid}")
    @Operation(summary = "Obtener todos los viajes por UID de usuario",
            description = "Devuelve la lista de viajes asociados al UID del usuario")
    public ResponseEntity<?> obtenerViajesPorUID(
            @Parameter(description = "UID del usuario", required = true)
            @PathVariable String uid) {
        try {
            List<Travel> viajes = service.findAllByUid(uid);
            if (viajes == null || viajes.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(viajes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("mensaje", "Error al obtener viajes: " + e.getMessage()));
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
        try{
          // Comprobar si el usuario ya tiene una reserva temporal activa
          ReservationTempDTO existing = reservationTempService.getByUID(req.getUserUid());
          if (existing != null) {
            // Usuario ya tiene una reserva temporal; no crear nueva
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("mensaje", "EL_USUARIO_YA_TIENE_RESERVA_PREVIA"));
          }
        }
        catch (Exception e){
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body(Collections.singletonMap("mensaje", "Error al verificar reservas previas: " + e.getMessage()));
        }

        try {
          Map<String, Object> blockResp = userClientRest.isBlockedForTravel(req.getUserUid());
          Boolean isBlocked = (Boolean) blockResp.get("blocked");
          if(isBlocked) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("mensaje", "USUARIO_BLOQUEADO_PARA_VIAJAR"));
          }
        } catch (Exception ex) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body(Collections.singletonMap("mensaje", "Error al verificar estado del usuario: " + ex.getMessage()));
        }

        String bikeType = req.getBikeType().trim().toUpperCase(Locale.ROOT);
        ResponseEntity<String> slotStartId;
        ResponseEntity<String> slotEndId;
        try {
            if ("ELECTRIC".equals(bikeType)) {
                slotStartId = slotsClient.reserveFirstAvailableElectric(req.getStationStartId());
            } else if ("MECHANIC".equals(bikeType)) {
                slotStartId = slotsClient.reserveFirstAvailableMechanic(req.getStationStartId());
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
          slotsClient.lockSlotById(slotStartId.getBody());

          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Collections.singletonMap("mensaje", "Error al solicitar slot: " + ex.getMessage()));
        }

        if (slotStartId == null || !slotStartId.getStatusCode().is2xxSuccessful() || slotStartId.getBody() == null || slotStartId.getBody().isEmpty()) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("mensaje", "No hay slots disponibles en la estación solicitada"));
        }

        String travelType;
        try {
            ResponseEntity<String> stationType = stationClient.getTypeById(req.getStationEndId());
            if (("METRO").equals(stationType.getBody())){
              travelType = "LAST MILE";
            }else {
              travelType = "LONG TRAVEL";
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("mensaje", "Error al obtener tipo de estación: " + ex.getMessage()));
        }

        // Separar slotId y bicycleId del cuerpo recibido (formato: "slotId|bicycleId")
        String slotIdRaw = slotStartId.getBody();
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
        dto.setTravelType(travelType);
        dto.setCreatedAt(System.currentTimeMillis());


        reservationTempService.save(dto);

        TravelReservationDTO travelStartDTO = new TravelReservationDTO();
        travelStartDTO.setUserId(req.getUserUid());
        travelStartDTO.setReservationId(reservationId);
        travelStartDTO.setStationStartId(req.getStationStartId());
        travelStartDTO.setStationEndId(req.getStationEndId());
        travelStartDTO.setSlotStartId(slotId);
        travelStartDTO.setTravelType(travelType);

        travelPublisher.sendJsonTravelReservationMessage(travelStartDTO);

        Map<String, String> resp = new HashMap<>();
        resp.put("reservationId", reservationId);
        resp.put("slotId", slotId);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/verify-bicycle")
    @Operation(summary = "Verificar bicicleta por código",
            description = "Recibe el UID del usuario y un código de 6 dígitos. Busca la reserva temporal del usuario en Redis y verifica que el código coincida con los últimos 6 dígitos del ID de la bicicleta. Si coincide, remueve las claves de la reserva en Redis. (La persistencia en BD se omite por ahora).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Verificación exitosa"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos o bicicleta equivocada"),
                    @ApiResponse(responseCode = "404", description = "Reserva no encontrada o expirada")
            })
    public ResponseEntity<?> verifyBicycleCode(
            @Parameter(description = "UID del usuario", required = true) @RequestParam("uid") String uid,
            @Parameter(description = "Código de verificación de 6 dígitos", example = "123456", required = true) @RequestParam("code") String code
    ) {
        // Validaciones básicas
        if (uid == null || uid.isBlank() || code == null || code.length() != 6 || !code.matches("\\d{6}")) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Parámetros inválidos: uid y code(6 dígitos) son requeridos"));
        }

        // Obtener reserva temporal por UID
        ReservationTempDTO dto = reservationTempService.getByUID(uid);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "Reserva no encontrada o expirada"));
        }
        String bicycleId = dto.getBicycleId();
        if (bicycleId == null || bicycleId.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "BicycleId inválido o no presente en la reserva"));
        }
        String lastSix = bicycleId.substring(bicycleId.length() - 6);
        if (!code.equals(lastSix)) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "BICICLETA_EQUIVOCADA", "detalle", "El código no coincide con la bicicleta reservada"));
        }

        // Remover claves en Redis: necesitamos el UUID de la reserva para usar reservationTempService.remove(reservationId)
        String storedKey = dto.getReservationId();
        String reservationUuid = extractReservationUuidFromKey(storedKey);
        if (reservationUuid == null) {
            // fallback: intentar quitar usando la clave base que se conoce
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "No se pudo determinar el identificador de la reserva para limpieza"));
        }
        try {
            reservationTempService.remove(storedKey);
            dto.setReservationId(extractReservationUuidFromKey(dto.getReservationId()));
            slotsClient.unlockSlotById(dto.getSlotStartId(), bicycleId);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error al remover la reserva temporal: " + ex.getMessage()));
        }

        // Crear y persistir el Travel en la base de datos
        try {
            Travel travel = new Travel();
            // requiredAt: usar createdAt de la reserva (epoch millis -> LocalDateTime)
            LocalDateTime requiredAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(dto.getCreatedAt()), ZoneId.systemDefault());
            travel.setRequiredAt(requiredAt);
            // startedAt: ahora
            travel.setStartedAt(LocalDateTime.now());
            travel.setEndedAt(null);
            travel.setStatus("IN_PROGRESS");
            travel.setUid(dto.getUserId());
            travel.setIdBicycle(bicycleId);
            travel.setFromIdStation(dto.getStationStartId());
            travel.setToIdStation(dto.getStationEndId());
            travel.setTravelType(dto.getTravelType() != null ? dto.getTravelType() : "MECHANIC");

            Travel saved = service.save(travel);

          reservationTempService.saveOnlyResources(dto);

          TravelStartDTO travelStartDTO = new TravelStartDTO();
          travelStartDTO.setUserId(dto.getUserId());
          travelStartDTO.setTravelId(dto.getReservationId());
          travelStartDTO.setStationStartId(dto.getStationStartId());
          travelStartDTO.setStationEndId(dto.getStationEndId());
          travelStartDTO.setSlotEndId(dto.getSlotEndId());
          travelStartDTO.setTravelType(dto.getTravelType());

          travelPublisher.sendJsonTravelStartMessage(travelStartDTO);

            return ResponseEntity.ok(Map.of(
                    "status", "OK",
                    "message", "Verificación exitosa, claves removidas y viaje guardado",
                    "travel", saved
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error al guardar el travel: " + ex.getMessage()));
        }
    }



    // Extrae el UUID de una clave como: reservation_temp:{UUID}:user_id:{UID}:data
    private String extractReservationUuidFromKey(String key) {
        if (key == null) return null;
        try {
            // Buscar el prefijo y separar por ":user_id:"
            int prefixIdx = key.indexOf("reservation_temp:");
            int userIdx = key.indexOf(":user_id:");
            if (prefixIdx != -1 && userIdx != -1 && userIdx > prefixIdx) {
                String between = key.substring(prefixIdx + "reservation_temp:".length(), userIdx);
                // Validación simple de UUID
                java.util.UUID.fromString(between);
                return between;
            }
        } catch (Exception ignored) { }
        return null;
    }


    private ResponseEntity<Map<String, String>> validate(BindingResult result) {
        Map<String,String> errors = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
