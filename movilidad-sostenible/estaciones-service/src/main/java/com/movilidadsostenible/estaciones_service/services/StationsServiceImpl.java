package com.movilidadsostenible.estaciones_service.services;

import com.movilidadsostenible.estaciones_service.model.entity.Station;
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
    public List<Station> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Station> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Station save(Station station) {
        return repository.save(station);
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}

