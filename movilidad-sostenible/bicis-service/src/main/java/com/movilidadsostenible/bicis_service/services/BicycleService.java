package com.movilidadsostenible.bicis_service.services;

import com.movilidadsostenible.bicis_service.model.entity.Bicycle;

import java.util.List;
import java.util.Optional;

public interface BicycleService {
    List<Bicycle> listBicycle();
    Optional<Bicycle> byId(Integer id);
    Bicycle save(Bicycle bicycle);
    void delete(Integer id);
}
