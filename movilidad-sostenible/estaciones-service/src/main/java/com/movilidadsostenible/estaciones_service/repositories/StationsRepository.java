package com.movilidadsostenible.estaciones_service.repositories;

import com.movilidadsostenible.estaciones_service.models.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StationsRepository extends JpaRepository<Station, Integer> {
}

