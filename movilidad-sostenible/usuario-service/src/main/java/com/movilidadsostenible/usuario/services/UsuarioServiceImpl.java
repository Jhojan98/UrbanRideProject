package com.movilidadsostenible.usuario.services;

import com.movilidadsostenible.usuario.clients.FineClient;
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

    @Autowired
    private FineClient fineClient;

    @Override
    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return (List<User>)repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> byId(String uid) {
        return repository.findById(uid);
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
    public void delete(String uid) {
        repository.deleteById(uid);
    }

    @Override
    public Integer getBalance(String uidUser) {
        return byId(uidUser).map(User::getBalance).orElse(null);
    }

    @Override
    @Transactional
    public Integer addBalance(String uidUser, Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("El monto debe ser positivo");
        }
        User user = byId(uidUser).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
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
        User user = byId(uidUser).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Integer current = user.getBalance() == null ? 0 : user.getBalance();
        if (current < amount) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }
        user.setBalance(current - amount);
        repository.save(user);
        return user.getBalance();
    }

    // Devuelve true si el usuario NO puede viajar (bloqueado), false si SÍ puede
    @Override
    @Transactional(readOnly = true)
    public boolean isUserBlockedForTravel(String uidUser) {
        Optional<User> userOpt = byId(uidUser);
        if (userOpt.isEmpty()) {
            // Si no existe el usuario, lo tratamos como bloqueado
            return true;
        }
        User user = userOpt.get();
        Integer balance = user.getBalance();
        boolean hasNegativeOrNullBalance = (balance == null || balance < 0);

        boolean hasUnpaidFines = false;
        try {
            hasUnpaidFines = fineClient.hasUnpaidFines(uidUser);
        } catch (Exception ex) {
            // En caso de error llamando al servicio de multas, por seguridad bloqueamos
            hasUnpaidFines = true;
        }

        // Bloqueado si tiene saldo negativo/nulo o multas impagas
        return hasNegativeOrNullBalance || hasUnpaidFines;
    }
}
