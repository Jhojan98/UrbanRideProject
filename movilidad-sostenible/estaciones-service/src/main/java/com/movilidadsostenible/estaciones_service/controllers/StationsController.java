package com.movilidadsostenible.estaciones_service.controllers;

import com.movilidadsostenible.estaciones_service.clients.CiudadClient;
import com.movilidadsostenible.estaciones_service.models.entity.Stations;
import com.movilidadsostenible.estaciones_service.services.StationsService;
import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/stations")
@Tag(name = "Estaciones", description = "CRUD de estaciones")
public class StationsController {

    private final StationsService service;
    private final CiudadClient ciudadClient;

    public StationsController(StationsService service, CiudadClient ciudadClient) {
        this.service = service;
        this.ciudadClient = ciudadClient;
    }

    @GetMapping
    @Operation(summary = "Listar estaciones")
    public ResponseEntity<List<Stations>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener estación por id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Encontrada",
                            content = @Content(schema = @Schema(implementation = Stations.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrada")
            })
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        Optional<Stations> opt = service.findById(id);
        return opt.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear estación")
    public ResponseEntity<?> create(@Valid @RequestBody Stations station, BindingResult result) {
        if (result.hasErrors()) return validate(result);
        // Verificar ciudad vía Feign
        try {
            var resp = ciudadClient.getCityById(station.getIdCity());
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "La ciudad especificada no existe"));
            }
        } catch (FeignException.NotFound nf) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", "La ciudad especificada no existe"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of(
                            "mensaje", "No fue posible validar la ciudad en ciudad-service",
                            "detalle", ex.getMessage()
                    ));
        }
        Stations saved = service.save(station);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estación")
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @Valid @RequestBody Stations station,
                                    BindingResult result) {
        if (result.hasErrors()) return validate(result);
        Optional<Stations> opt = service.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        // Verificar ciudad si cambia o siempre validar
        try {
            var resp = ciudadClient.getCityById(station.getIdCity());
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("mensaje", "La ciudad especificada no existe"));
            }
        } catch (FeignException.NotFound nf) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", "La ciudad especificada no existe"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of(
                            "mensaje", "No fue posible validar la ciudad en ciudad-service",
                            "detalle", ex.getMessage()
                    ));
        }
        Stations db = opt.get();
        db.setStationName(station.getStationName());
        db.setStreet(station.getStreet());
        db.setAvenue(station.getAvenue());
        db.setNumber(station.getNumber());
        db.setIdCity(station.getIdCity());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(db));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar estación")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        Optional<Stations> opt = service.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Map<String, String>> validate(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(err -> errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}
