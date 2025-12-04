package com.movilidadsostenible.viaje_service.repositories;

import com.movilidadsostenible.viaje_service.models.entity.Travel;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TravelRepository extends CrudRepository<Travel, Integer> {
    // Método para obtener todos los viajes de un usuario por su UID
    List<Travel> findAllByUid(String uid);
}

