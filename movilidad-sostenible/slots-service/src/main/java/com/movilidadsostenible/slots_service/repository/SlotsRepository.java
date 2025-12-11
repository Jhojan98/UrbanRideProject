package com.movilidadsostenible.slots_service.repository;

import com.movilidadsostenible.slots_service.model.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlotsRepository extends JpaRepository<Slot, String> {
    Optional<Slot> findFirstByStationIdAndPadlockStatusOrderByIdSlotAsc(Integer stationId, String padlockStatus);

    @Query("""
    SELECT s FROM Slot s
    WHERE s.stationId = :stationId
      AND s.padlockStatus = :padlockStatus
      AND s.bicycleId LIKE CONCAT(:prefix, '%')
    ORDER BY s.idSlot ASC
    """)
    Optional<Slot> findFirstAvailableSlotByPrefix(
            Integer stationId,
            String padlockStatus,
            String prefix);

}
