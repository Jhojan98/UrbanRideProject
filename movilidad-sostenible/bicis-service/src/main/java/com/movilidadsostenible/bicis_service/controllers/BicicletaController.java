package com.movilidadsostenible.bicis_service.controllers;

import com.movilidadsostenible.bicis_service.model.entity.Bicycle;
import com.movilidadsostenible.bicis_service.services.BicycleService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@Tag(name = "Bicicletas", description = "Operaciones CRUD para bicicletas")
public class BicicletaController {

    @Autowired
    private BicycleService service;

    @GetMapping
    @Operation(summary = "Listar bicicletas", description = "Obtiene el listado completo de bicicletas")
    public ResponseEntity<List<Bicycle>> listBicycles() {
        List<Bicycle> bicicletas = service.listBicycle();
        return ResponseEntity.ok(bicicletas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener bicicleta por id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Bicicleta encontrada",
                            content = @Content(schema = @Schema(implementation = Bicycle.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrada")
            })
    public ResponseEntity<?> getBicycleByid(
            @Parameter(description = "Identificador de la bicicleta", required = true)
            @PathVariable Integer id) {
        Optional<Bicycle> o = service.byId(id);
        if (o.isPresent()) {
            return ResponseEntity.ok(o.get());
        } else {
            return ResponseEntity.notFound().build();
        }

    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear bicicleta",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Creada",
                            content = @Content(schema = @Schema(implementation = Bicycle.class))),
                    @ApiResponse(responseCode = "400", description = "Validación fallida")
            })
    public ResponseEntity<?> createBicycle(@Valid @RequestBody Bicycle bicycle,
                                           BindingResult result) {
        if (result.hasErrors()) {
            return validate(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(bicycle));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar bicicleta")
    public ResponseEntity<?> updateBicycle(@Valid @RequestBody Bicycle bicycle,
                                           BindingResult result,
                                           @PathVariable Integer id) {
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar bicicleta")
    public ResponseEntity<?> deleteBicycle(@PathVariable Integer id) {
        Optional<Bicycle> o = service.byId(id);
        if (o.isPresent()) {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private ResponseEntity<Map<String, String>> validate(BindingResult result) {
        Map<String,String> errors = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
