package com.movilidadsostenible.viaje_service.services;

import com.movilidadsostenible.viaje_service.models.dto.ReservationTempDTO;

public interface ReservationTempService {
    void save(ReservationTempDTO dto);
    void saveOnlyResources(ReservationTempDTO dto);
    ReservationTempDTO getExpired(String reservationId);
    ReservationTempDTO get(String reservationId);
    ReservationTempDTO getByUID(String userId);
    void removeExpired(String reservationId);
    void remove(String reservationId);
    void releaseResources(ReservationTempDTO dto);
}
