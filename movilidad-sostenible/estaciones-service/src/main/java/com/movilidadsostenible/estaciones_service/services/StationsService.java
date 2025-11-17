package com.movilidadsostenible.estaciones_service.services;

import com.movilidadsostenible.estaciones_service.models.entity.Stations;

import java.util.List;
import java.util.Optional;

public interface StationsService {
    List<Stations> findAll();
    Optional<Stations> findById(Integer id);
    Stations save(Stations station);
    void deleteById(Integer id);
}

