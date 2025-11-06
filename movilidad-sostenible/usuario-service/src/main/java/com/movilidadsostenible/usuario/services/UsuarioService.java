package com.movilidadsostenible.usuario.services;

import com.movilidadsostenible.usuario.models.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<Usuario> listarUsuarios();
    Optional<Usuario> porId(Integer id);
    Usuario guardar(Usuario usuario);
    void eliminar(Integer id);

    Optional<Usuario> porCorreoElectronico(String email);
}
