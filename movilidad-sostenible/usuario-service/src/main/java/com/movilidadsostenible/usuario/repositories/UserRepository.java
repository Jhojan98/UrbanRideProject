package com.movilidadsostenible.usuario.repositories;

import com.movilidadsostenible.usuario.models.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Integer> {
    Optional<User> findByUidUser(String uidUser);
}
