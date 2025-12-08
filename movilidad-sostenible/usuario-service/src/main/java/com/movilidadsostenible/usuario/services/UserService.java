package com.movilidadsostenible.usuario.services;

import com.movilidadsostenible.usuario.models.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> listUsers();
    User save(User user);
    void delete(String uid);
    Optional<User> byId(String uidUser);

    Double getBalance(String uidUser);
    Double addBalance(String uidUser, Double amount);
    Double subtractBalance(String uidUser, Double amount);

    // Devuelve true si el usuario NO puede viajar (bloqueado), false si SÍ puede
    boolean isUserBlockedForTravel(String uidUser);

    // Cobra el viaje aplicando reglas de suscripción.
    void chargeTravel(Double totalTripValue, Integer excessMinutes, String uidUser);
}
