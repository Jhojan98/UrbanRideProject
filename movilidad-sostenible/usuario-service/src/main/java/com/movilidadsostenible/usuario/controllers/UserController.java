package com.movilidadsostenible.usuario.controllers;

import com.movilidadsostenible.usuario.models.entity.User;
import com.movilidadsostenible.usuario.services.UserService;
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
@Tag(name = "Usuarios", description = "Operaciones CRUD para usuarios")
public class UserController {

    @Autowired
    private UserService service;

    @GetMapping
    @Operation(summary = "Listar usuarios")
    public List<User> listUsers() {
        return service.listUsers();
    }

    @GetMapping("/login/{uid}")
    @Operation(summary = "Obtener usuario por UID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Encontrado",
                            content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrado")
            })
    public ResponseEntity<?> getUserById(
            @Parameter(description = "Identificador del usuario", required = true)
            @PathVariable String uid) {
        Optional<User> usuarioOptional = service.byId(uid);
        return usuarioOptional.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Creado",
                            content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "400", description = "Validación fallida")
            })
    public ResponseEntity<?> createUser(@Valid @RequestBody User user,
                                        BindingResult result) {
        if (result.hasErrors()) {
            return validate(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<?> updateUser(@Valid @RequestBody User user,
                                        BindingResult result,
                                        @PathVariable String uid) {
        if (result.hasErrors()) {
            return validate(result);
        }
        Optional<User> usuarioOptional = service.byId(uid);
        if(usuarioOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User usuarioDB = usuarioOptional.get();
        usuarioDB.setUserName(user.getUserName());
        usuarioDB.setSubscriptionType(user.getSubscriptionType());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(usuarioDB));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<?> deleteUser(@PathVariable String uid) {
        Optional<User> usuarioOptional = service.byId(uid);
        if (usuarioOptional.isPresent()) {
            service.delete(uid);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // --- Endpoints de balance ---
    @GetMapping("/balance/{uid}")
    @Operation(summary = "Obtener balance de usuario")
    public ResponseEntity<?> getBalance(@PathVariable("uid") String uidUser) {
        Integer balance = service.getBalance(uidUser);
        if (balance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Usuario no encontrado"));
        }
        return ResponseEntity.ok(Map.of("uid", uidUser, "balance", balance));
    }

    @PostMapping("/balance/{uid}/add")
    @Operation(summary = "Agregar saldo al usuario")
    public ResponseEntity<?> addBalance(@PathVariable("uid") String uidUser,
                                        @RequestParam("amount") Integer amount) {
        try {
            Integer newBalance = service.addBalance(uidUser, amount);
            return ResponseEntity.ok(Map.of("uid", uidUser, "balance", newBalance));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/balance/{uid}/subtract")
    @Operation(summary = "Quitar saldo al usuario")
    public ResponseEntity<?> subtractBalance(@PathVariable("uid") String uidUser,
                                             @RequestParam("amount") Integer amount) {
        try {
            Integer newBalance = service.subtractBalance(uidUser, amount);
            return ResponseEntity.ok(Map.of("uid", uidUser, "balance", newBalance));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/travel/blocked/{uid}")
    @Operation(summary = "Consultar si el usuario está bloqueado para viajar",
            description = "Devuelve true si el usuario NO puede viajar (saldo negativo/nulo o multas impagas), false si SÍ puede viajar")
    public ResponseEntity<?> isBlockedForTravel(
            @Parameter(description = "UID del usuario", required = true)
            @PathVariable("uid") String uidUser
    ) {
        boolean blocked = service.isUserBlockedForTravel(uidUser);
        return ResponseEntity.ok(Map.of("uid", uidUser, "blocked", blocked));
    }

    // --- Cobro de viaje ---
    @PostMapping("/travel/charge/{uid}")
    @Operation(summary = "Cobrar viaje al usuario",
            description = "Cobra el viaje aplicando reglas de suscripción: si es MONTLY descuenta viajes, si es NONE descuenta balance y puede quedar negativo")
    public ResponseEntity<?> chargeTravel(
            @Parameter(description = "UID del usuario", required = true)
            @PathVariable("uid") String uidUser,
            @Parameter(description = "Valor total del viaje", required = true)
            @RequestParam("total") Integer totalTripValue,
            @Parameter(description = "Minutos excedentes", required = true)
            @RequestParam("excessMinutes") Integer excessMinutes
    ) {
        try {
            service.chargeTravel(totalTripValue, excessMinutes, uidUser);
            return ResponseEntity.ok(Map.of(
                    "uid", uidUser,
                    "charged", true
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }




    private ResponseEntity<Map<String, String>> validate(BindingResult result) {
        Map<String,String> errores = new HashMap<>();
        result.getFieldErrors().forEach(err -> errores.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errores);
    }
}
