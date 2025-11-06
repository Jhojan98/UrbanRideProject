package com.movilidadsostenible.viaje_service.repositories;

import com.movilidadsostenible.viaje_service.models.entity.Viaje;
import org.springframework.data.repository.CrudRepository;

public interface ViajeRepository extends CrudRepository<Viaje, String> {
}
