package com.movilidadsostenible.viaje_service.services;

import com.movilidadsostenible.viaje_service.models.Usuario;
import com.movilidadsostenible.viaje_service.models.entity.Viaje;

import java.util.List;
import java.util.Optional;

public interface ViajeService {
    List<Viaje> listarViajes();
    Optional<Viaje> porId(String id);
    Viaje guardar(Viaje viaje);
    void eliminar(String id);

}
