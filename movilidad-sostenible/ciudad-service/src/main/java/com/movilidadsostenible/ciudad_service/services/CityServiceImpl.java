package com.movilidadsostenible.ciudad_service.services;

import com.movilidadsostenible.ciudad_service.models.entity.City;
import com.movilidadsostenible.ciudad_service.repositories.CityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CityServiceImpl implements CityService {

    private final CityRepository repository;

    public CityServiceImpl(CityRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<City> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<City> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public City save(City city) {
        return repository.save(city);
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}

