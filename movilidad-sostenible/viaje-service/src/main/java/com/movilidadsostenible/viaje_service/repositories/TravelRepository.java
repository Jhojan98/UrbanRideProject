package com.movilidadsostenible.viaje_service.repositories;

import com.movilidadsostenible.viaje_service.models.entity.Travel;
import org.springframework.data.repository.CrudRepository;

public interface TravelRepository extends CrudRepository<Travel, Integer> {
}
