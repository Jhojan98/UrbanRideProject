package com.movilidadsostenible.usuario.controllers;

import com.movilidadsostenible.usuario.models.entity.Usuario;
import com.movilidadsostenible.usuario.services.UsuarioService;
import com.movilidadsostenible.usuario.services.UsuarioServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Optional;

@RestController
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @GetMapping("/usuarios")
    public List<Usuario> listarUsuarios() {
        return service.listarUsuarios();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable String id) {
        Optional<Usuario> usuarioOptional = service.porId(id);

        if(usuarioOptional.isPresent()) {
            return ResponseEntity.ok(usuarioOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario crearusuario(@RequestBody Usuario usuario) {
        return service.guardar(usuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@RequestBody Usuario usuario, @PathVariable String id) {
        Optional<Usuario> usuarioOptional = service.porId(id);
        if(usuarioOptional.isPresent()) {
            Usuario usuarioDB = usuarioOptional.get();

            usuarioDB.setN_primerNombre(usuario.getN_primerNombre());
            usuarioDB.setN_segundoNombre(usuario.getN_segundoNombre());
            usuarioDB.setN_primerApellido(usuario.getN_primerApellido());
            usuarioDB.setN_segundoApellido(usuario.getN_segundoApellido());
            usuarioDB.setF_fechaNacimiento(usuario.getF_fechaNacimiento());
            usuarioDB.setN_correoElectronico(usuario.getN_correoElectronico());
            usuarioDB.setT_tipoSuscripcion(usuario.getT_tipoSuscripcion());
            usuarioDB.setF_fechaRegistro(usuario.getF_fechaRegistro());

            return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(usuarioDB));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable String id) {
        Optional<Usuario> usuarioOptional = service.porId(id);
        if (usuarioOptional.isPresent()) {
            service.eliminar(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
