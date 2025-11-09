package com.movilidadsostenible.usuario.services;

import com.movilidadsostenible.usuario.models.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> listUsers();
    Optional<User> byId(Integer id);
    User save(User user);
    void delete(Integer id);

    Optional<User> byUserEmail(String userEmail);
    void updateVerificationStatus(Integer userCc, boolean verified);

}
