package com.movilidadsostenible.slots_service.controller;

import com.movilidadsostenible.slots_service.clients.BicycleClient;
import com.movilidadsostenible.slots_service.model.entity.Slot;
import com.movilidadsostenible.slots_service.service.SlotsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Slots", description = "Operaciones para gestionar slots de estación")
public class SlotsController {

    @Autowired
    private BicycleClient bicycleClient;

    private final SlotsService service;

    public SlotsController(SlotsService service) {
        this.service = service;
    }

    @PostMapping("/create")
    @Operation(
            summary = "Crear un slot",
            description = "Crea un slot con identificador, estado del candado, estación y bicicleta opcional."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Slot creado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Slot.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content)
    })
    public ResponseEntity<Slot> create(
            @RequestBody(description = "Datos del slot a crear", required = true,
                    content = @Content(schema = @Schema(implementation = Slot.class)))
            @org.springframework.web.bind.annotation.RequestBody Slot slot
    ) {
        Slot created = service.create(slot);
        return ResponseEntity.created(URI.create("/api/v1/slots/" + created.getIdSlot())).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener slot por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slot encontrado",
                    content = @Content(schema = @Schema(implementation = Slot.class))),
            @ApiResponse(responseCode = "404", description = "Slot no encontrado", content = @Content)
    })
    public ResponseEntity<Slot> getById(
            @Parameter(description = "Identificador del slot", example = "S-001")
            @PathVariable String id
    ) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Listar slots")
    @ApiResponse(responseCode = "200", description = "Listado de slots",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Slot.class))))
    public List<Slot> getAll() {
        return service.findAll();
    }

    @PutMapping("/{id}/padlock-status")
    @Operation(summary = "Actualizar estado del candado",
            description = "Actualiza el estado del candado del slot (por ejemplo, LOCKED o UNLOCKED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado",
                    content = @Content(schema = @Schema(implementation = Slot.class))),
            @ApiResponse(responseCode = "404", description = "Slot no encontrado", content = @Content)
    })
    public ResponseEntity<Slot> updatePadlockStatus(
            @Parameter(description = "Identificador del slot", example = "S-001")
            @PathVariable String id,
            @Parameter(description = "Nuevo estado del candado", example = "UNLOCKED")
            @RequestParam String padlockStatus
    ) {
        return ResponseEntity.ok(service.updatePadlockStatus(id, padlockStatus));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar slot por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Eliminado", content = @Content)
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador del slot", example = "S-001")
            @PathVariable String id
    ) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // reservar primer slot disponible (LOCKED -> RESERVED) devolviendo con bicicleta MECHANIC (bicycleId = "MECH-******) slotId y bicycleId en texto plano
    @PostMapping("/stations/{stationId}/reserve-mechanic")
    @Operation(summary = "Reservar el primer slot LOCKED (bicicleta mecánica)",
            description = "Devuelve el primer slot con estado LOCKED de la estación indicada y lo cambia a RESERVED. La respuesta incluye slotId y bicycleId concatenados por '|', por ejemplo: EST-TIP-01|MECH-123456.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ID del slot y bicycleId",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "EST-TIP-01|MECH-123456"))),
            @ApiResponse(responseCode = "400", description = "No hay slots LOCKED disponibles", content = @Content)
    })
    public ResponseEntity<String> reserveFirstAvailable(
            @Parameter(description = "Identificador de la estación", example = "1")
            @PathVariable Integer stationId
    ) {
        try {
            Slot reserved = service.reserveFirstAvailableSlotMechanicBicy(stationId);
            String payload = reserved.getIdSlot() + "|" + reserved.getBicycleId();
            try {
                bicycleClient.updatePadlockStatus(reserved.getBicycleId(), "RESERVED");
            } catch (Exception feignEx) {
                return ResponseEntity.status(502)
                        .body("BICIS_SERVICE_ERROR: " + feignEx.getMessage());
            }
            return ResponseEntity.ok(payload);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("NO_SLOT_AVAILABLE");
        }
    }

    // reservar primer slot disponible (LOCKED -> RESERVED) devolviendo con bicicleta ELECTRIC (bicycleId = "ELEC-******) slotId y bicycleId en texto plano
    @PostMapping("/stations/{stationId}/reserve-electric")
    @Operation(summary = "Reservar el primer slot LOCKED (bicicleta eléctrica)",
            description = "Devuelve el ID del primer slot con estado LOCKED de la estación indicada y cambia su estado a RESERVED. La respuesta incluye slotId y bicycleId concatenados por '|', por ejemplo: EST-TIP-01|ELEC-123456.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ID del slot y bicycleId",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "EST-TIP-01|ELEC-123456"))),
            @ApiResponse(responseCode = "400", description = "No hay slots LOCKED disponibles", content = @Content)
    })
    public ResponseEntity<String> reserveFirstAvailableElectric(
            @Parameter(description = "Identificador de la estación", example = "1")
            @PathVariable Integer stationId
    ) {
        try {
            Slot reserved = service.reserveFirstAvailableSlotElectricBicy(stationId);
            String payload = reserved.getIdSlot() + "|" + reserved.getBicycleId();
            try {
                bicycleClient.updatePadlockStatus(reserved.getBicycleId(), "RESERVED");
            } catch (Exception feignEx) {
                return ResponseEntity.status(502)
                        .body("BICIS_SERVICE_ERROR: " + feignEx.getMessage());
            }
            return ResponseEntity.ok(payload);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("NO_SLOT_AVAILABLE");
        }
    }

    // reservar primer slot disponible (UNLOCKED -> RESERVED) devolviendo solo el ID
    @PostMapping("/stations/{stationId}/reserve-first-unlocked")
    @Operation(summary = "Reservar el primer slot UNLOCKED de una estación",
            description = "Devuelve el ID del primer slot con estado UNLOCKED de la estación indicada y cambia su estado a RESERVED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ID del slot reservado",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "EST-TIP-02"))),
            @ApiResponse(responseCode = "400", description = "No hay slots UNLOCKED disponibles", content = @Content)
    })
    public ResponseEntity<String> reserveFirstUnlocked(
            @Parameter(description = "Identificador de la estación", example = "1")
            @PathVariable Integer stationId
    ) {
        try {
            Slot reserved = service.reserveFirstUnlockedSlot(stationId);
            return ResponseEntity.ok(reserved.getIdSlot());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("NO_UNLOCKED_SLOT_AVAILABLE");
        }
    }

    // Nuevo endpoint: bloquear slot asignando bicicleta (padlockStatus -> LOCKED)
    @PostMapping("/{slotId}/lock")
    @Operation(summary = "Bloquear un slot asignando una bicicleta",
            description = "Cambia el estado del candado a LOCKED y asigna el ID de la bicicleta al slot indicado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slot bloqueado",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "S-001"))),
            @ApiResponse(responseCode = "404", description = "Slot no encontrado", content = @Content)
    })
    public ResponseEntity<String> lockSlotWithBicycle(
            @Parameter(description = "Identificador del slot", example = "S-001")
            @PathVariable String slotId,
            @Parameter(description = "Identificador de la bicicleta", example = "123")
            @RequestParam String bicycleId
    ) {
        try {
            Slot locked = service.lockSlotWithBicycle(slotId, bicycleId);
            return ResponseEntity.ok(locked.getIdSlot());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT: Bloquear un slot por ID (LOCKED) y actualizar la bicicleta asociada
    @PutMapping("/{slotId}/lock")
    @Operation(summary = "Bloquear slot por ID",
            description = "Cambia el estado del candado del slot a LOCKED y, si hay bicicleta asociada, actualiza su padlockStatus a LOCKED en bicis-service.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slot bloqueado",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "S-001|ELEC-123456"))),
            @ApiResponse(responseCode = "404", description = "Slot no encontrado", content = @Content)
    })
    public ResponseEntity<String> lockSlotById(
            @Parameter(description = "Identificador del slot", example = "S-001")
            @PathVariable String slotId
    ) {
        // Obtener slot
        Slot slot = service.findById(slotId).orElse(null);
        if (slot == null) {
            return ResponseEntity.notFound().build();
        }
        // Cambiar estado del slot a LOCKED
        slot = service.updatePadlockStatus(slotId, "LOCKED");

        // Intentar actualizar la bicicleta asociada
        String bicycleId = slot.getBicycleId();
        if (bicycleId != null && !bicycleId.isEmpty()) {
            try {
                bicycleClient.updatePadlockStatus(bicycleId, "LOCKED");
            } catch (Exception feignEx) {
                return ResponseEntity.status(502)
                        .body("BICIS_SERVICE_ERROR: " + feignEx.getMessage());
            }
        }
        String payload = slot.getIdSlot() + (bicycleId != null ? ("|" + bicycleId) : "");
        return ResponseEntity.ok(payload);
    }

}
