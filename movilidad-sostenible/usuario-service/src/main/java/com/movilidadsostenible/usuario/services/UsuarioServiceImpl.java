package com.movilidadsostenible.usuario.services;

import com.movilidadsostenible.usuario.models.entity.User;
import com.movilidadsostenible.usuario.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return (List<User>)repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> byId(Integer id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public User save(User user) {
        // Inicializar balance si viene null
        if (user.getBalance() == null) {
            user.setBalance(0);
        }
        return repository.save(user);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<User> byUid(String uidUser) {
        return repository.findByUidUser(uidUser);
    }

    @Override
    public Integer getBalance(String uidUser) {
        return byUid(uidUser).map(User::getBalance).orElse(null);
    }

    @Override
    @Transactional
    public Integer addBalance(String uidUser, Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("El monto debe ser positivo");
        }
        User user = byUid(uidUser).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Integer current = user.getBalance() == null ? 0 : user.getBalance();
        user.setBalance(current + amount);
        repository.save(user);
        return user.getBalance();
    }

    @Override
    @Transactional
    public Integer subtractBalance(String uidUser, Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("El monto debe ser positivo");
        }
        User user = byUid(uidUser).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Integer current = user.getBalance() == null ? 0 : user.getBalance();
        if (current < amount) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }
        user.setBalance(current - amount);
        repository.save(user);
        return user.getBalance();
    }
}
