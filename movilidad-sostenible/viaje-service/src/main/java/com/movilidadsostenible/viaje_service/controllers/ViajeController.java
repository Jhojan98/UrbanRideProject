package com.movilidadsostenible.viaje_service.controllers;

import com.movilidadsostenible.viaje_service.models.entity.Viaje;
import com.movilidadsostenible.viaje_service.services.ViajeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class ViajeController {
    @Autowired
    private ViajeService service;

    @GetMapping("/viajes")
    public List<Viaje> listarViajes() {
        return service.listarViajes();
    }

    @GetMapping("/viaje/{id}")
    public ResponseEntity<?> obtenerViajesPorId(@PathVariable String id) {
        Optional<Viaje> usuarioOptional = service.porId(id);

        if(usuarioOptional.isPresent()) {
            return ResponseEntity.ok(usuarioOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/viaje/usuario/{id}")
    public ResponseEntity<?> obtenerViajesPorIdUsuario(@PathVariable String id) {
        Optional<Viaje> usuarioOptional = service.porId(id);

        if(usuarioOptional.isPresent()) {
            return ResponseEntity.ok(usuarioOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/viaje/bicicleta/{id}")
    public ResponseEntity<?> obtenerViajesPorIdBicicleta(@PathVariable String id) {
        Optional<Viaje> usuarioOptional = service.porId(id);

        if(usuarioOptional.isPresent()) {
            return ResponseEntity.ok(usuarioOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> crearViaje(@Valid @RequestBody Viaje viaje,
                                          BindingResult result) {

        if (result.hasErrors()) {
            return validar(result);
        }
        try{
            Optional<Viaje> viajeGuardado = Optional.ofNullable(service.guardar(viaje));
            if (viajeGuardado.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("mensaje", "No se pudo crear el viaje. Verifique que el usuario y la bicicleta existan."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("mensaje", "Error al crear el viaje: " + e.getMessage()));
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarViaje(@PathVariable String id) {
        Optional<Viaje> usuarioOptional = service.porId(id);
        if (usuarioOptional.isPresent()) {
            service.eliminar(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    private ResponseEntity<Map<String, String>> validar(BindingResult result) {
        Map<String,String> errores = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errores.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errores);
    }
}
