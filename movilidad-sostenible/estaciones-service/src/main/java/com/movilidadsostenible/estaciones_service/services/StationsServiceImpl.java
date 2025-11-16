package com.movilidadsostenible.estaciones_service.services;

import com.movilidadsostenible.estaciones_service.models.entity.Stations;
import com.movilidadsostenible.estaciones_service.repositories.StationsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StationsServiceImpl implements StationsService {

    private final StationsRepository repository;

    public StationsServiceImpl(StationsRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Stations> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Stations> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Stations save(Stations station) {
        return repository.save(station);
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}

