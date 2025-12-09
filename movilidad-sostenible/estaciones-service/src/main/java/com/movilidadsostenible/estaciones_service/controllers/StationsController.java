package com.movilidadsostenible.estaciones_service.controllers;

import com.movilidadsostenible.estaciones_service.clients.CiudadClient;
import com.movilidadsostenible.estaciones_service.clients.SlotsClient;
import com.movilidadsostenible.estaciones_service.model.dto.SlotRequestDTO;
import com.movilidadsostenible.estaciones_service.model.entity.Station;
import com.movilidadsostenible.estaciones_service.services.StationsService;
import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@CrossOrigin(origins = "*")
@Tag(name = "Estaciones", description = "CRUD de estaciones y creación automática de slots")
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
    @Operation(
            summary = "Listar estaciones",
            description = "Devuelve la lista completa de estaciones registradas.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de estaciones",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Station.class))))
            }
    )
    public ResponseEntity<List<Station>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener estación por id",
            description = "Busca una estación por su identificador numérico.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estación encontrada",
                            content = @Content(schema = @Schema(implementation = Station.class))),
                    @ApiResponse(responseCode = "404", description = "Estación no encontrada")
            }
    )
    public ResponseEntity<Station> getById(@PathVariable Integer id) {
        Optional<Station> opt = service.findById(id);
        return ResponseEntity.of(opt);
    }

    // Nuevo endpoint: obtener solo el campo `type` de la estación por id
    @GetMapping("/{id}/type")
    @Operation(summary = "Obtener tipo de estación por id", description = "Devuelve el campo `type` de la estación indicada por id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tipo de estación obtenido",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Estación no encontrada")
            }
    )
    public ResponseEntity<String> getTypeById(@PathVariable Integer id) {
        Optional<Station> opt = service.findById(id);
        // Devolver únicamente la cadena del tipo (por ejemplo "METRO") en el cuerpo como texto plano
        return opt.map(st -> ResponseEntity.ok().contentType(org.springframework.http.MediaType.TEXT_PLAIN).body(st.getType()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(
            summary = "Crear estación",
            description = "Crea una nueva estación y genera automáticamente 15 slots asociados. Valida que la ciudad exista mediante ciudad-service.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto Station a crear",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Station.class),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Ejemplo de solicitud",
                                            summary = "Estación METRO en ciudad 1",
                                            value = "{\n  \"idStation\": 1,\n  \"stationName\": \"POLO\",\n  \"latitude\": 0,\n  \"length\": 0,\n  \"idCity\": 1,\n  \"type\": \"METRO\",\n  \"cctvStatus\": true\n}"
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Estación creada correctamente",
                            content = @Content(
                                    schema = @Schema(implementation = Station.class),
                                    examples = {
                                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                                    name = "Respuesta de creación",
                                                    summary = "Estructura simplificada",
                                                    value = "{\n  \"estacion\": {\n    \"idStation\": 1,\n    \"stationName\": \"POLO\",\n    \"latitude\": 0,\n    \"length\": 0,\n    \"idCity\": 1,\n    \"type\": \"METRO\",\n    \"cctvStatus\": true\n  },\n  \"slotsGenerados\": [\n    { \"slotId\": \"POL-MET-1\", \"status\": 201 },\n    { \"slotId\": \"POL-MET-2\", \"status\": 201 }\n  ]\n}"
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida o la ciudad especificada no existe"),
                    @ApiResponse(responseCode = "502", description = "No fue posible validar la ciudad en ciudad-service"),
                    @ApiResponse(responseCode = "500", description = "Error interno al crear slots o estación")
            }
    )
    public ResponseEntity<?> create(@Valid @RequestBody Station station,
                                    BindingResult result) {
        System.out.println("Creando estación: " + station);
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
        Station saved = service.save(station);

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
                SlotRequestDTO slotReq = new SlotRequestDTO();

                slotReq.setIdSlot(slotId);
                slotReq.setPadlockStatus("UNLOCKED");
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
    @Operation(
            summary = "Actualizar estación",
            description = "Actualiza los datos de una estación existente. Valida ciudad mediante ciudad-service.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Objeto Station con los cambios", required = true,
                    content = @Content(schema = @Schema(implementation = Station.class))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Estación actualizada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida o la ciudad especificada no existe"),
                    @ApiResponse(responseCode = "404", description = "Estación no encontrada"),
                    @ApiResponse(responseCode = "502", description = "No fue posible validar la ciudad en ciudad-service")
            }
    )
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @Valid @RequestBody Station station,
                                    BindingResult result) {
        if (result.hasErrors()) return validate(result);
        Optional<Station> opt = service.findById(id);
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
        Station db = opt.get();
        db.setStationName(station.getStationName());
        db.setIdCity(station.getIdCity());
        db.setType(station.getType());
        db.setLatitude(station.getLatitude());
        db.setLength(station.getLength());

        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(db));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar estación",
            description = "Elimina la estación indicada por id. No elimina recursos remotos (ej. slots) automáticamente.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Estación eliminada correctamente"),
                    @ApiResponse(responseCode = "404", description = "Estación no encontrada")
            }
    )
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        Optional<Station> opt = service.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cities/{idCity}/stations")
    @Operation(
            summary = "Listar estaciones por ciudad",
            description = "Devuelve todas las estaciones que pertenecen a la ciudad indicada por su id.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de estaciones",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Station.class))))
            }
    )
    public ResponseEntity<List<Station>> getByCity(@PathVariable Integer idCity) {
        List<Station> stations = service.findByIdCity(idCity);
        return ResponseEntity.ok(stations);
    }



    private ResponseEntity<Map<String, String>> validate(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(err -> errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}
