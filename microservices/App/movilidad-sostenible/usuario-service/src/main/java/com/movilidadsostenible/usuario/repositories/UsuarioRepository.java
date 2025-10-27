package com.movilidadsostenible.usuario.repositories;

import com.movilidadsostenible.usuario.models.entity.Usuario;
import org.springframework.data.repository.CrudRepository;

public interface UsuarioRepository extends CrudRepository<Usuario, String> {
}
