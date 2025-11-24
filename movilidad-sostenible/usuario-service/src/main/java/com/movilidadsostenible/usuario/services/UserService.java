package com.movilidadsostenible.usuario.services;

import com.movilidadsostenible.usuario.models.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> listUsers();
    User save(User user);
    void delete(String uid);
    Optional<User> byId(String uidUser);

    Integer getBalance(String uidUser);
    Integer addBalance(String uidUser, Integer amount);
    Integer subtractBalance(String uidUser, Integer amount);
}
