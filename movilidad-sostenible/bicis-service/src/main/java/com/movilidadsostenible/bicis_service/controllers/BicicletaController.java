package com.movilidadsostenible.bicis_service.controllers;

import com.movilidadsostenible.bicis_service.model.entity.Bicycle;
import com.movilidadsostenible.bicis_service.services.BicycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@Tag(name = "Bicicletas", description = "Operaciones CRUD para bicicletas")
public class BicicletaController {

    @Autowired
    private BicycleService service;

    private static final SecureRandom RANDOM = new SecureRandom();

    @GetMapping(produces = "application/json")
    @Operation(summary = "Listar bicicletas",
            description = "Obtiene el listado completo de bicicletas registradas")
    @ApiResponse(responseCode = "200", description = "Listado obtenido",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Bicycle.class))))
    public ResponseEntity<List<Bicycle>> listBicycles() {
        List<Bicycle> bicicletas = service.listBicycle();
        return ResponseEntity.ok(bicicletas);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @Operation(summary = "Obtener bicicleta por id",
            description = "Retorna la bicicleta con el identificador proporcionado")
    @ApiResponse(responseCode = "200", description = "Bicicleta encontrada",
            content = @Content(schema = @Schema(implementation = Bicycle.class)))
    @ApiResponse(responseCode = "404", description = "No encontrada")
    public ResponseEntity<?> getBicycleByid(
            @Parameter(description = "Identificador de la bicicleta", required = true, example = "B-001")
            @PathVariable String id) {
        Optional<Bicycle> o = service.byId(id);
        if (o.isPresent()) {
            return ResponseEntity.ok(o.get());
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Actualizar bicicleta",
            description = "Actualiza campos de la bicicleta indicada por id")
    @ApiResponse(responseCode = "201", description = "Actualizada",
            content = @Content(schema = @Schema(implementation = Bicycle.class)))
    @ApiResponse(responseCode = "404", description = "No encontrada")
    public ResponseEntity<?> updateBicycle(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Cuerpo con los cambios a aplicar",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Bicycle.class)))
            @Valid @RequestBody Bicycle bicycle,
            BindingResult result,
            @Parameter(description = "Identificador de la bicicleta a actualizar", example = "B-001")
            @PathVariable String id) {
        Optional<Bicycle> o = service.byId(id);
        if (o.isPresent()) {
            Bicycle bicicletaDb = o.get();
            bicicletaDb.setSeries(bicycle.getSeries());
            bicicletaDb.setModel(bicycle.getModel());
            bicicletaDb.setPadlockStatus(bicycle.getPadlockStatus());
            bicicletaDb.setLastUpdate(bicycle.getLastUpdate());
            bicicletaDb.setLatitude(bicycle.getLatitude());
            bicicletaDb.setLength(bicycle.getLength());
            bicicletaDb.setBattery(bicycle.getBattery());
            service.save(bicicletaDb);
            return ResponseEntity.status(HttpStatus.CREATED).body(bicicletaDb);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Eliminar bicicleta",
            description = "Elimina la bicicleta por identificador")
    @ApiResponse(responseCode = "204", description = "Eliminada")
    @ApiResponse(responseCode = "404", description = "No encontrada")
    public ResponseEntity<?> deleteBicycle(
            @Parameter(description = "Identificador de la bicicleta a eliminar", example = "B-001")
            @PathVariable String id) {
        Optional<Bicycle> o = service.byId(id);
        if (o.isPresent()) {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Crear una bicicleta eléctrica (model = ELECTRIC, id = ELEC-######)
    @PostMapping(value = "/electric", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear bicicleta eléctrica",
            description = "Crea una bicicleta con modelo ELECTRIC y un id autogenerado ELEC-######")
    @ApiResponse(responseCode = "201", description = "Creada",
            content = @Content(schema = @Schema(implementation = Bicycle.class)))
    @ApiResponse(responseCode = "400", description = "Validación fallida")
    public ResponseEntity<?> createElectric(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos base de la bicicleta eléctrica (sin id ni model)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Bicycle.class),
                            examples = @ExampleObject(value = "{\n  \"series\": 2026,\n  \"padlockStatus\": \"UNLOCKED\"}")))
            @Valid @RequestBody Bicycle bicycle,
            BindingResult result) {
        if (result.hasErrors()) {
            return validate(result);
        }
        bicycle.setIdBicycle(generateId("ELEC"));
        bicycle.setModel("ELECTRIC");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(bicycle));
    }

    // Crear una bicicleta mecánica (model = MECHANIC, id = MECH-######)
    @PostMapping(value = "/mechanic", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear bicicleta mecánica",
            description = "Crea una bicicleta con modelo MECHANIC y un id autogenerado MECH-######")
    @ApiResponse(responseCode = "201", description = "Creada",
            content = @Content(schema = @Schema(implementation = Bicycle.class)))
    @ApiResponse(responseCode = "400", description = "Validación fallida")
    public ResponseEntity<?> createMechanic(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos base de la bicicleta mecánica (sin id ni model)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Bicycle.class),
                            examples = @ExampleObject(value = "{\n  \"series\": 2025,\n  \"padlockStatus\": \"UNLOCKED\"\n}")))
            @Valid @RequestBody Bicycle bicycle,
            BindingResult result) {
        if (result.hasErrors()) {
            return validate(result);
        }
        bicycle.setIdBicycle(generateId("MECH"));
        bicycle.setModel("MECHANIC");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(bicycle));
    }

    // Metodo para actualizar el padlockeStatus de una bicicleta
    @PutMapping(value = "/{id}/padlock-status", produces = "application/json")
    @Operation(summary = "Actualizar estado del candado de una bicicleta",
            description = "Actualiza el campo padlockStatus de la bicicleta indicada por id. Valores típicos: LOCKED, UNLOCKED")
    @ApiResponse(responseCode = "200", description = "Actualizada",
            content = @Content(schema = @Schema(implementation = Bicycle.class)))
    @ApiResponse(responseCode = "404", description = "No encontrada")
    public ResponseEntity<?> updatePadlockStatus(
            @Parameter(description = "Identificador de la bicicleta", required = true, example = "ELEC-123456")
            @PathVariable String id,
            @Parameter(description = "Nuevo estado del candado", required = true, example = "LOCKED")
            @RequestParam("status") String status) {
        Optional<Bicycle> o = service.byId(id);
        if (o.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Bicycle bici = o.get();
        bici.setPadlockStatus(status);
        service.save(bici);
        return ResponseEntity.ok(bici);
    }

    private String generateId(String prefix) {
        int number = RANDOM.nextInt(1_000_000); // 0..999999
        return String.format("%s-%06d", prefix, number);
    }

    private ResponseEntity<Map<String, String>> validate(BindingResult result) {
        Map<String,String> errors = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
