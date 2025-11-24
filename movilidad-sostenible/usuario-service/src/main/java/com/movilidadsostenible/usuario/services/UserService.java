package com.movilidadsostenible.usuario.services;

import com.movilidadsostenible.usuario.models.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> listUsers();
    Optional<User> byId(Integer id);
    User save(User user);
    void delete(Integer id);

    // Nuevos métodos de balance
    Optional<User> byUid(String uidUser);
    Integer getBalance(String uidUser);
    Integer addBalance(String uidUser, Integer amount);
    Integer subtractBalance(String uidUser, Integer amount);
}
