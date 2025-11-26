package com.movilidadsostenible.estaciones_service.services;

import com.movilidadsostenible.estaciones_service.models.entity.Station;

import java.util.List;
import java.util.Optional;

public interface StationsService {
    List<Station> findAll();
    Optional<Station> findById(Integer id);
    Station save(Station station);
    void deleteById(Integer id);
}

