package com.movilidadsostenible.ciudad_service.controllers;

import com.movilidadsostenible.ciudad_service.models.entity.City;
import com.movilidadsostenible.ciudad_service.services.CityService;
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
@RequestMapping("/cities")
@Tag(name = "Ciudades", description = "CRUD de ciudades")
public class CityController {

    private final CityService service;

    public CityController(CityService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar ciudades")
    public ResponseEntity<List<City>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ciudad por id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Encontrada",
                            content = @Content(schema = @Schema(implementation = City.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrada")
            })
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        Optional<City> opt = service.findById(id);
        return opt.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear ciudad")
    public ResponseEntity<?> create(@Valid @RequestBody City city, BindingResult result) {
        if (result.hasErrors()) return validate(result);
        City saved = service.save(city);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ciudad")
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @Valid @RequestBody City city,
                                    BindingResult result) {
        if (result.hasErrors()) return validate(result);
        Optional<City> opt = service.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        City db = opt.get();
        db.setCityName(city.getCityName());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(db));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ciudad")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        Optional<City> opt = service.findById(id);
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

