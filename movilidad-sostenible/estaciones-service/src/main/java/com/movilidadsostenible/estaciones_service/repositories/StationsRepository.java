package com.movilidadsostenible.estaciones_service.repositories;

import com.movilidadsostenible.estaciones_service.model.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationsRepository extends JpaRepository<Station, Integer> {
    List<Station> findByIdCity(Integer idCity);
}
