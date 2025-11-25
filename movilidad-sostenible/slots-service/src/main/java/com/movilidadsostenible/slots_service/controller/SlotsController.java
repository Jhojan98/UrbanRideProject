package com.movilidadsostenible.slots_service.controller;

import com.movilidadsostenible.slots_service.model.entity.Slot;
import com.movilidadsostenible.slots_service.service.SlotsService;
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

    @PatchMapping("/{id}/padlock-status")
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
}
