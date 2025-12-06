package com.movilidadsostenible.usuario.services;

import com.movilidadsostenible.usuario.models.entity.User;

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

    // Devuelve true si el usuario NO puede viajar (bloqueado), false si SÍ puede
    boolean isUserBlockedForTravel(String uidUser);

    // Cobra el viaje aplicando reglas de suscripción.
    void chargeTravel(Integer totalTripValue, Integer excessMinutes, String uidUser);
}
