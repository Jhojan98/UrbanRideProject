package com.movilidadsostenible.viaje_service.services;

import com.movilidadsostenible.viaje_service.models.entity.Travel;

import java.util.List;
import java.util.Optional;

public interface TravelService {
    List<Travel> listTravels();
    Optional<Travel> byId(Integer id);
    Travel save(Travel travel);
    void delete(Integer id);

}
