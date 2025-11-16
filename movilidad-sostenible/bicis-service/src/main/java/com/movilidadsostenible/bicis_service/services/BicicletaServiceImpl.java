package com.movilidadsostenible.bicis_service.services;

import com.movilidadsostenible.bicis_service.entity.Bicycle;
import com.movilidadsostenible.bicis_service.repositories.BicycleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BicicletaServiceImpl implements BicycleService {

    @Autowired
    private BicycleRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<Bicycle> listBicycle() {
        return (List<Bicycle>) repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Bicycle> byId(Integer id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public Bicycle save(Bicycle bicycle) {
        return repository.save(bicycle);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
