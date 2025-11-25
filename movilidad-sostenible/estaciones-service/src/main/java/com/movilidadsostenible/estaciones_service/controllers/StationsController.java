package com.movilidadsostenible.estaciones_service.controllers;

import com.movilidadsostenible.estaciones_service.clients.CiudadClient;
import com.movilidadsostenible.estaciones_service.clients.SlotsClient;
import com.movilidadsostenible.estaciones_service.clients.SlotRequest;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping
@Tag(name = "Estaciones", description = "CRUD de estaciones")
public class StationsController {

    private final StationsService service;
    private final CiudadClient ciudadClient;
    private final SlotsClient slotsClient;

    public StationsController(StationsService service, CiudadClient ciudadClient, SlotsClient slotsClient) {
        this.service = service;
        this.ciudadClient = ciudadClient;
        this.slotsClient = slotsClient;
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
    public ResponseEntity<?> create(@Valid @RequestBody Stations station,
                                    BindingResult result) {
        if (result.hasErrors()) return validate(result);
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

        // Prefijos robustos (manejo de nulos y longitud < 3)
        String stationName = saved.getStationName() != null ? saved.getStationName().trim() : "EST";
        if (stationName.isEmpty()) stationName = "EST";
        String typeValue = saved.getType() != null ? saved.getType().trim() : "GEN";
        if (typeValue.isEmpty()) typeValue = "GEN";

        String stationPrefix = stationName.length() >= 3 ? stationName.substring(0,3).toUpperCase() : stationName.toUpperCase();
        String typePrefix = typeValue.length() >= 3 ? typeValue.substring(0,3).toUpperCase() : typeValue.toUpperCase();

        var slotsCreados = new java.util.ArrayList<Map<String,Object>>();
        for (int i = 1; i <= 15; i++) {
            String slotId = stationPrefix + "-" + typePrefix + "-" + i;
            try {
                SlotRequest slotReq = new SlotRequest();
                slotReq.setSlotId(slotId);
                slotReq.setPadlockStatus("LOCKED");
                slotReq.setStationId(saved.getIdStation());
                slotReq.setBicycleId(null);
                ResponseEntity<?> slotResp = slotsClient.createSlot(slotReq);
                Map<String,Object> info = new HashMap<>();
                info.put("slotId", slotId);
                info.put("status", slotResp.getStatusCode().value());
                slotsCreados.add(info);
            } catch (Exception e) {
                Map<String,Object> errorInfo = new HashMap<>();
                errorInfo.put("slotId", slotId);
                errorInfo.put("error", e.getMessage());
                slotsCreados.add(errorInfo);
            }
        }
        Map<String,Object> respuesta = new HashMap<>();
        respuesta.put("estacion", saved);
        respuesta.put("slotsGenerados", slotsCreados);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estación")
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @Valid @RequestBody Stations station,
                                    BindingResult result) {
        if (result.hasErrors()) return validate(result);
        Optional<Stations> opt = service.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
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
        db.setIdCity(station.getIdCity());
        db.setType(station.getType());
        db.setLatitude(station.getLatitude());
        db.setLength(station.getLength());

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
