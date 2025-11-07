package com.movilidadsostenible.bicis_service.services;

import com.movilidadsostenible.bicis_service.entity.Bicicleta;

import java.util.List;
import java.util.Optional;

public interface BicicletaService {
    List<Bicicleta> listarBicicletas();
    Optional<Bicicleta> porId(Integer id);
    Bicicleta guardar(Bicicleta bicicleta);
    void eliminar(Integer id);
}
