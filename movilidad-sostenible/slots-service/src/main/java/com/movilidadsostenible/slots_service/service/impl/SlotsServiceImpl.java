package com.movilidadsostenible.slots_service.service.impl;

import com.movilidadsostenible.slots_service.model.entity.Slot;
import com.movilidadsostenible.slots_service.repository.SlotsRepository;
import com.movilidadsostenible.slots_service.service.SlotsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SlotsServiceImpl implements SlotsService {

    private final SlotsRepository repository;

    public SlotsServiceImpl(SlotsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Slot create(Slot slot) {
        return repository.save(slot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Slot> findById(String id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Slot> findAll() {
        return repository.findAll();
    }

    @Override
    public Slot updatePadlockStatus(String id, String padlockStatus) {
        Slot slot = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Slot no encontrado: " + id));
        slot.setPadlockStatus(padlockStatus);
        return repository.save(slot);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }
}
