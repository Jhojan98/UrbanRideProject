package com.movilidadsostenible.viaje_service.controllers;

import com.movilidadsostenible.viaje_service.models.entity.Travel;
import com.movilidadsostenible.viaje_service.services.TravelService;
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

@RestController
@Tag(name = "Viajes", description = "Operaciones para gestionar viajes")
public class TravelController {
    @Autowired
    private TravelService service;

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


    private ResponseEntity<Map<String, String>> validate(BindingResult result) {
        Map<String,String> errors = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
