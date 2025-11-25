package com.movilidadsostenible.slots_service.service;

import com.movilidadsostenible.slots_service.model.entity.Slot;
import java.util.List;
import java.util.Optional;

public interface SlotsService {
    Slot create(Slot slot);
    Optional<Slot> findById(String id);
    List<Slot> findAll();
    Slot updatePadlockStatus(String id, String padlockStatus);
    void delete(String id);
}
