package com.movilidadsostenible.bicis_service.services;

import com.movilidadsostenible.bicis_service.entity.Bicicleta;
import com.movilidadsostenible.bicis_service.repositories.BicicletaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BicicletaServiceImpl implements BicicletaService {

    @Autowired
    private BicicletaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<Bicicleta> listarBicicletas() {
        return (List<Bicicleta>) repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Bicicleta> porId(Integer id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public Bicicleta guardar(Bicicleta bicicleta) {
        return repository.save(bicicleta);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        repository.deleteById(id);
    }
}
