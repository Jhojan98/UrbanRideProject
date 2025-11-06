package com.movilidadsostenible.usuario.controllers;

import com.movilidadsostenible.usuario.models.entity.Usuario;
import com.movilidadsostenible.usuario.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import java.util.*;

@RestController
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @GetMapping("/usuarios")
    public List<Usuario> listarUsuarios() {
        return service.listarUsuarios();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable Integer id) {
        Optional<Usuario> usuarioOptional = service.porId(id);

        if(usuarioOptional.isPresent()) {
            return ResponseEntity.ok(usuarioOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> crearUsuario(@Valid @RequestBody Usuario usuario,
                                          BindingResult result) {
        if (!usuario.getCorreoElectronico().isEmpty() &&
                service.porCorreoElectronico(usuario.getCorreoElectronico()).isPresent()){
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("mensaje", "Ya!!! existe un usuario con ese correo electrónico"));
        }

        if (result.hasErrors()) {
            return validar(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@Valid @RequestBody Usuario usuario, 
                                               BindingResult result,
                                               @PathVariable Integer id) {


        if (result.hasErrors()) {
            return validar(result);
        }
        
        Optional<Usuario> usuarioOptional = service.porId(id);
        if(usuarioOptional.isPresent()) {
            Usuario usuarioDB = usuarioOptional.get();

            if (!usuario.getCorreoElectronico().isEmpty() &&
                    !usuario.getCorreoElectronico().equalsIgnoreCase(usuarioDB.getCorreoElectronico()) &&
                    service.porCorreoElectronico(usuario.getCorreoElectronico()).isPresent()){
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("mensaje", "Ya existe un usuario con ese correo electrónico cambielo"));
            }

            usuarioDB.setPrimerNombre(usuario.getPrimerNombre());
            usuarioDB.setSegundoNombre(usuario.getSegundoNombre());
            usuarioDB.setPrimerApellido(usuario.getPrimerApellido());
            usuarioDB.setSegundoApellido(usuario.getSegundoApellido());
            usuarioDB.setFechaNacimiento(usuario.getFechaNacimiento());
            usuarioDB.setCorreoElectronico(usuario.getCorreoElectronico());
            usuarioDB.setTipoSuscripcion(usuario.getTipoSuscripcion());
            usuarioDB.setFechaRegistro(usuario.getFechaRegistro());

            return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(usuarioDB));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Integer id) {
        Optional<Usuario> usuarioOptional = service.porId(id);
        if (usuarioOptional.isPresent()) {
            service.eliminar(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    private ResponseEntity<Map<String, String>> validar(BindingResult result) {
        Map<String,String> errores = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errores.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errores);
    }
}

