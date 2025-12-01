package com.movilidadsostenible.admin.controllers;

import com.movilidadsostenible.admin.models.entity.Admin;
import com.movilidadsostenible.admin.services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Tag(name = "Administradores", description = "Operaciones CRUD para administradores")
public class AdminController {

    @Autowired
    private AdminService service;

    @GetMapping
    @Operation(summary = "Listar administradores")
    public List<Admin> listUsers() {
        return service.listAdmins();
    }

    @GetMapping("/login/{uid}")
    @Operation(summary = "Obtener administrador por UID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Encontrado",
                            content = @Content(schema = @Schema(implementation = Admin.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrado")
            })
    public ResponseEntity<?> getUserById(
            @Parameter(description = "Identificador del administrador", required = true)
            @PathVariable String uid) {
        Optional<Admin> usuarioOptional = service.byId(uid);
        return usuarioOptional.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar administrador",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Creado",
                            content = @Content(schema = @Schema(implementation = Admin.class))),
                    @ApiResponse(responseCode = "400", description = "Validación fallida")
            })
    public ResponseEntity<?> createUser(@Valid @RequestBody Admin user,
                                        BindingResult result) {
        if (result.hasErrors()) {
            return validate(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar administrador")
    public ResponseEntity<?> updateUser(@Valid @RequestBody Admin user,
                                        BindingResult result,
                                        @PathVariable String uid) {
        if (result.hasErrors()) {
            return validate(result);
        }
        Optional<Admin> usuarioOptional = service.byId(uid);
        if(usuarioOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Admin usuarioDB = usuarioOptional.get();
        usuarioDB.setUserName(user.getUserName());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(usuarioDB));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar administrador")
    public ResponseEntity<?> deleteUser(@PathVariable String uid) {
        Optional<Admin> usuarioOptional = service.byId(uid);
        if (usuarioOptional.isPresent()) {
            service.delete(uid);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<Map<String, String>> validate(BindingResult result) {
        Map<String,String> errores = new HashMap<>();
        result.getFieldErrors().forEach(err -> errores.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errores);
    }
}
