package com.movilidadsostenible.ciudad_service.services;

import com.movilidadsostenible.ciudad_service.models.entity.City;

import java.util.List;
import java.util.Optional;

public interface CityService {
    List<City> findAll();
    Optional<City> findById(Integer id);
    City save(City city);
    void deleteById(Integer id);
}

