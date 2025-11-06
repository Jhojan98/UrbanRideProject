package com.movilidadsostenible.bicis_service.controllers;

import com.movilidadsostenible.bicis_service.entity.Bicicleta;
import com.movilidadsostenible.bicis_service.services.BicicletaService;
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
public class BicicletaController {

    @Autowired
    private BicicletaService service;

    @GetMapping
    public ResponseEntity<List<Bicicleta>> listarBicicletas() {
        List<Bicicleta> bicicletas = service.listarBicicletas();
        return ResponseEntity.ok(bicicletas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerBicicletaPorId(@PathVariable Integer id) {
        Optional<Bicicleta> o = service.porId(id);
        if (o.isPresent()) {
            return ResponseEntity.ok(o.get());
        } else {
            return ResponseEntity.notFound().build();
        }

    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> crearusuario(@Valid @RequestBody Bicicleta bicicleta,
                                  BindingResult result) {
        if (result.hasErrors()) {
            return validar(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(bicicleta));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarBicicleta(@Valid @RequestBody Bicicleta bicicleta,
                                                 BindingResult result,
                                                 @PathVariable Integer id) {
        Optional<Bicicleta> o = service.porId(id);
        if (o.isPresent()) {
            Bicicleta bicicletaDb = o.get();
            bicicletaDb.setSerie(bicicleta.getSerie());
            bicicletaDb.setModelo(bicicleta.getModelo());
            bicicletaDb.setEstadoCandado(bicicleta.getEstadoCandado());
            bicicletaDb.setUltimaActualizacion(bicicleta.getUltimaActualizacion());
            bicicletaDb.setLatitud(bicicleta.getLatitud());
            bicicletaDb.setLongitud(bicicleta.getLongitud());
            bicicletaDb.setBateria(bicicleta.getBateria());
            service.guardar(bicicletaDb);
            return ResponseEntity.status(HttpStatus.CREATED).body(bicicletaDb);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarBicicleta(@PathVariable Integer id) {
        Optional<Bicicleta> o = service.porId(id);
        if (o.isPresent()) {
            service.eliminar(id);
            return ResponseEntity.noContent().build();
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
