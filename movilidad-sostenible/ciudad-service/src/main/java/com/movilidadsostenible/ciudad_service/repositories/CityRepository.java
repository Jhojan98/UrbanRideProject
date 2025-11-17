package com.movilidadsostenible.ciudad_service.repositories;

import com.movilidadsostenible.ciudad_service.models.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {
}

