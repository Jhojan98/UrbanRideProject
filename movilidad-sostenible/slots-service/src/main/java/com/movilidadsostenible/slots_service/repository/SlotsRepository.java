package com.movilidadsostenible.slots_service.repository;

import com.movilidadsostenible.slots_service.model.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlotsRepository extends JpaRepository<Slot, String> {
}
