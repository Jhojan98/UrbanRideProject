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

        if(usuarioOptional.isPresent()) {
            return ResponseEntity.ok(usuarioOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
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
        if (!user.getUserEmail().isEmpty() &&
                service.byUserEmail(user.getUserEmail()).isPresent()){
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("mensaje", "Ya!!! existe un user con ese correo electrónico"));
        }

        if (result.hasErrors()) {
            return validate(result);
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setUserCc(user.getUserCc());
        userDTO.setUserEmail(user.getUserEmail());
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
        if(usuarioOptional.isPresent()) {
            User usuarioDB = usuarioOptional.get();

            if (!user.getUserEmail().isEmpty() &&
                    !user.getUserEmail().equalsIgnoreCase(usuarioDB.getUserEmail()) &&
                    service.byUserEmail(user.getUserEmail()).isPresent()){
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("mensaje", "Ya existe un usuario con ese correo electrónico cambielo"));
            }

            usuarioDB.setFirstName(user.getFirstName());
            usuarioDB.setSecondName(user.getSecondName());
            usuarioDB.setFirstLastname(user.getFirstLastname());
            usuarioDB.setSecondLastname(user.getSecondLastname());
            usuarioDB.setUserBirthday(user.getUserBirthday());
            usuarioDB.setUserEmail(user.getUserEmail());
            usuarioDB.setSubscriptionType(user.getSubscriptionType());
            usuarioDB.setUserRegistrationDate(user.getUserRegistrationDate());
            usuarioDB.setIsVerified(user.getIsVerified());

            return ResponseEntity.status(HttpStatus.CREATED).body(service.save(usuarioDB));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        Optional<User> usuarioOptional = service.byId(id);
        if (usuarioOptional.isPresent()) {
            service.delete(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{userCc}/verification/resend")
    @Operation(summary = "Reenviar código de verificación", description = "Genera y envía nuevamente el código de verificación al correo del usuario.",
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
        // Publicar nuevamente el mensaje al email-service para regenerar OTP
        UserDTO userDTO = new UserDTO();
        userDTO.setUserCc(usuario.getUserCc());
        userDTO.setUserEmail(usuario.getUserEmail());
        userDTO.setVerified(false); // sigue sin verificar
        publisher.sendJsonMessage(userDTO);
        return ResponseEntity.ok(Map.of(
                "resent", true,
                "message", "Se solicitó el reenvío del código de verificación",
                "userCc", usuario.getUserCc()
        ));
    }


    private ResponseEntity<Map<String, String>> validate(BindingResult result) {
        Map<String,String> errores = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errores.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errores);
    }
}
