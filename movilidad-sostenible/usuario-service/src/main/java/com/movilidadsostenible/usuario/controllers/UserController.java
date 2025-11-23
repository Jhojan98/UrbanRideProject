package com.movilidadsostenible.usuario.controllers;

import com.movilidadsostenible.usuario.models.dto.UserDTO;
import com.movilidadsostenible.usuario.models.entity.User;
import com.movilidadsostenible.usuario.publisher.UserPublisher;
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

    private final UserPublisher publisher;

    public UserController(UserPublisher publisher) {
        this.publisher = publisher;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios")
    public List<User> listUsers() {
        return service.listUsers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Encontrado",
                            content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrado")
            })
    public ResponseEntity<?> getUserById(
            @Parameter(description = "Identificador del usuario", required = true)
            @PathVariable Integer id) {
        Optional<User> usuarioOptional = service.byId(id);
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
        // Publicar datos para verificación (sin email)
        UserDTO userDTO = new UserDTO();
        userDTO.setUserCc(user.getUserCc());
        userDTO.setVerified(user.getIsVerified());
        publisher.sendJsonMessage(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<?> updateUser(@Valid @RequestBody User user,
                                        BindingResult result,
                                        @PathVariable Integer id) {
        if (result.hasErrors()) {
            return validate(result);
        }
        Optional<User> usuarioOptional = service.byId(id);
        if(usuarioOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User usuarioDB = usuarioOptional.get();
        usuarioDB.setFirstName(user.getFirstName());
        usuarioDB.setSecondName(user.getSecondName());
        usuarioDB.setFirstLastname(user.getFirstLastname());
        usuarioDB.setSecondLastname(user.getSecondLastname());
        usuarioDB.setUserBirthday(user.getUserBirthday());
        usuarioDB.setSubscriptionType(user.getSubscriptionType());
        usuarioDB.setUserRegistrationDate(user.getUserRegistrationDate());
        usuarioDB.setIsVerified(user.getIsVerified());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(usuarioDB));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        Optional<User> usuarioOptional = service.byId(id);
        if (usuarioOptional.isPresent()) {
            service.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{userCc}/verification/resend")
    @Operation(summary = "Reenviar código de verificación", description = "Regenera y envía nuevamente el código de verificación.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OTP reenviado", content = @Content(schema = @Schema(implementation = Map.class))),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "409", description = "Usuario ya verificado")
            })
    public ResponseEntity<?> resendVerification(
            @Parameter(description = "Documento del usuario", required = true)
            @PathVariable Integer userCc) {
        Optional<User> usuarioOptional = service.byId(userCc);
        if (usuarioOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("resent", false, "message", "Usuario no encontrado"));
        }
        User usuario = usuarioOptional.get();
        if (Boolean.TRUE.equals(usuario.getIsVerified())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("resent", false, "message", "El usuario ya está verificado"));
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUserCc(usuario.getUserCc());
        userDTO.setVerified(false);
        publisher.sendJsonMessage(userDTO);
        return ResponseEntity.ok(Map.of(
                "resent", true,
                "message", "Se solicitó el reenvío del código de verificación",
                "userCc", usuario.getUserCc()
        ));
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

    private ResponseEntity<Map<String, String>> validate(BindingResult result) {
        Map<String,String> errores = new HashMap<>();
        result.getFieldErrors().forEach(err -> errores.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errores);
    }
}
