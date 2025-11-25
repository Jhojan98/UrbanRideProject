package com.movilidadsostenible.bicis_service.repositories;

import com.movilidadsostenible.bicis_service.model.entity.Bicycle;
import org.springframework.data.repository.CrudRepository;

public interface BicycleRepository extends CrudRepository<Bicycle, Integer> {
}
