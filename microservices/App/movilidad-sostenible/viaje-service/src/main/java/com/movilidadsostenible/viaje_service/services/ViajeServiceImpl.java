package com.movilidadsostenible.viaje_service.services;

import com.movilidadsostenible.viaje_service.clients.BicicletaClientRest;
import com.movilidadsostenible.viaje_service.clients.UsuarioClientRest;
import com.movilidadsostenible.viaje_service.models.Bicicleta;
import com.movilidadsostenible.viaje_service.models.Usuario;
import com.movilidadsostenible.viaje_service.models.entity.Viaje;
import com.movilidadsostenible.viaje_service.repositories.ViajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ViajeServiceImpl implements ViajeService {
    @Autowired
    private ViajeRepository repository;

    @Autowired
    private UsuarioClientRest usuarioClientRest;

    @Autowired
    private BicicletaClientRest bicicletaClientRest;

    @Override
    @Transactional(readOnly = true)
    public List<Viaje> listarViajes() {
        return (List<Viaje>)repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Viaje> porId(String id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public Viaje guardar(Viaje viaje) {
        Optional<Usuario> usuarioOptional = Optional.ofNullable(usuarioClientRest.detalleUsuario(viaje.getCedulaCiudadaniaUsuario()));
        Optional<Bicicleta> bicicletaOptional = Optional.ofNullable(bicicletaClientRest.detalleBicicleta(viaje.getIdBicicleta()));

        if (usuarioOptional.isPresent() && bicicletaOptional.isPresent()) {
            return repository.save(viaje);
        }
        return null;

    }

    @Override
    @Transactional
    public void eliminar(String id) {
        repository.deleteById(id);
    }


}
