package com.movilidadsostenible.viaje_service.services;

import com.movilidadsostenible.viaje_service.clients.BicycleClient;
import com.movilidadsostenible.viaje_service.clients.UserClientRest;
import com.movilidadsostenible.viaje_service.models.entity.Travel;
import com.movilidadsostenible.viaje_service.repositories.TravelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TravelServiceImpl implements TravelService {
    @Autowired
    private TravelRepository repository;

    @Autowired
    private UserClientRest userClientRest;

    @Autowired
    private BicycleClient bicycleClient;

    @Override
    @Transactional(readOnly = true)
    public List<Travel> listTravels() {
        return (List<Travel>)repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Travel> byId(Integer id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public Travel save(Travel travel) {
        return repository.save(travel);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Travel> findAllByUid(String uid) {
        return repository.findAllByUid(uid);
    }

    // Metodo para obtener un viaje a partir del bicycleId y que el t_status esté en "IN_PROGRESS"
    @Override
    @Transactional(readOnly = true)
    public Optional<Travel> findFirstByIdBicycleAndStatus(String idBicycle, String status) {
        return repository.findFirstByIdBicycleAndStatus(idBicycle, status);
    }

}
