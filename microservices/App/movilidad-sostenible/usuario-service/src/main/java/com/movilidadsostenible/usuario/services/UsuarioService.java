package com.movilidadsostenible.usuario.services;

import com.movilidadsostenible.usuario.models.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<Usuario> listarUsuarios();
    Optional<Usuario> porId(String id);
    Usuario guardar(Usuario usuario);
    void eliminar(String id);
}
