package com.movilidadsostenible.viaje_service.services;

import com.movilidadsostenible.viaje_service.models.dto.ReservationTempDTO;

public interface ReservationTempService {
    void save(ReservationTempDTO dto);
    ReservationTempDTO get(String reservationId);
    void remove(String reservationId);
    void releaseResources(ReservationTempDTO dto);
}
