package com.movilidadsostenible.bicis_service.controllers;

import com.movilidadsostenible.bicis_service.entity.Bicycle;
import com.movilidadsostenible.bicis_service.services.BicycleService;
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
    private BicycleService service;

    @GetMapping
    public ResponseEntity<List<Bicycle>> listBicycles() {
        List<Bicycle> bicicletas = service.listBicycle();
        return ResponseEntity.ok(bicicletas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBicycleByid(@PathVariable Integer id) {
        Optional<Bicycle> o = service.byId(id);
        if (o.isPresent()) {
            return ResponseEntity.ok(o.get());
        } else {
            return ResponseEntity.notFound().build();
        }

    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createBicycle(@Valid @RequestBody Bicycle bicycle,
                                           BindingResult result) {
        if (result.hasErrors()) {
            return validate(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(bicycle));
    }

    @PutMapping("/{id}")
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
