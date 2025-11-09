package com.movilidadsostenible.usuario.controllers;

import com.movilidadsostenible.usuario.models.dto.UserDTO;
import com.movilidadsostenible.usuario.models.entity.User;
import com.movilidadsostenible.usuario.publisher.UserPublisher;
import com.movilidadsostenible.usuario.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import java.util.*;

@RestController
public class UserController {

    @Autowired
    private UserService service;

    private final UserPublisher publisher;

    public UserController(UserPublisher publisher) {
        this.publisher = publisher;
    }

    @GetMapping
    public List<User> listUsers() {
        return service.listUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        Optional<User> usuarioOptional = service.byId(id);

        if(usuarioOptional.isPresent()) {
            return ResponseEntity.ok(usuarioOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/register")
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
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        Optional<User> usuarioOptional = service.byId(id);
        if (usuarioOptional.isPresent()) {
            service.delete(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    private ResponseEntity<Map<String, String>> validate(BindingResult result) {
        Map<String,String> errores = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errores.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errores);
    }
}

