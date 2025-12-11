package com.movilidadsostenible.viaje_service.repositories;

import com.movilidadsostenible.viaje_service.models.entity.Travel;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TravelRepository extends CrudRepository<Travel, Integer> {
    // Método para obtener todos los viajes de un usuario por su UID
    List<Travel> findAllByUid(String uid);

    // Metodo para obtener un viaje a partir del bicleID y que el t_satatus este en "IN_PROGRESS"
    Optional<Travel> findFirstByIdBicycleAndStatus(String idBicycle, String tStatus);

    // Get active trip by user UID
    Optional<Travel> findFirstByUidAndStatus(String uid, String status);
}
