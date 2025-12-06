package com.movilidadsostenible.usuario.services;

import com.movilidadsostenible.usuario.clients.FineClient;
import com.movilidadsostenible.usuario.models.entity.User;
import com.movilidadsostenible.usuario.repositories.UserRepository;
import com.movilidadsostenible.usuario.rabbit.publisher.UserPublisher;
import com.movilidadsostenible.usuario.models.dto.UserDTO;
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

    @Autowired
    private UserPublisher userPublisher;

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

        // Si tiene suscripción MONTLY y al menos 1 viaje disponible en subcripcionTravels,
        // el usuario NO está bloqueado independientemente del balance.
        String subscriptionType = user.getSubscriptionType();
        if ("MONTLY".equalsIgnoreCase(subscriptionType)) {
            Integer travelsAvailable = user.getSubcripcionTravels();
            if (travelsAvailable != null && travelsAvailable >= 1) {
                // Tiene por lo menos un viaje mensual disponible, está habilitado para viajar
                // (ignorando balance). Aún así, si tiene multas impagas, se bloquea.
                try {
                    boolean hasUnpaidFines = fineClient.hasUnpaidFines(uidUser);
                    return hasUnpaidFines; // true si bloqueado por multas, false si puede viajar
                } catch (Exception ex) {
                    // En caso de error consultando multas, por seguridad bloqueamos
                    return true;
                }
            }
        }

        Integer balance = user.getBalance();
        boolean hasNegativeOrNullBalance = (balance == null || balance < 0);

        boolean hasUnpaidFines;
        try {
            hasUnpaidFines = fineClient.hasUnpaidFines(uidUser);
        } catch (Exception ex) {
            // En caso de error llamando al servicio de multas, por seguridad bloqueamos
            hasUnpaidFines = true;
        }

        // Bloqueado si tiene saldo negativo/nulo o multas impagas
        return hasNegativeOrNullBalance || hasUnpaidFines;
    }

    @Override
    @Transactional
    public void chargeTravel(Integer totalTripValue, Integer excessMinutes, String uidUser) {
        if (totalTripValue == null || totalTripValue < 0) {
            throw new IllegalArgumentException("El valor total del viaje debe ser no negativo");
        }
        if (excessMinutes == null || excessMinutes < 0) {
            throw new IllegalArgumentException("Los minutos excedentes deben ser no negativos");
        }
        User user = byId(uidUser).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        String subscriptionType = user.getSubscriptionType();

        String subject;
        String message;

        if ("MONTLY".equalsIgnoreCase(subscriptionType)) {
            Integer travels = user.getSubcripcionTravels();
            if (travels == null) travels = 0;
            // Resta 1 por el viaje incluido
            travels -= 1;
            // Si hay minutos excedentes, resta 1 adicional
            if (excessMinutes > 0) {
                travels -= 1;
            }
            user.setSubcripcionTravels(travels);

            subject = "Cobro de viaje por suscripción MONTLY";
            message = String.format("Se cobró un viaje usando la suscripción mensual. Viajes restantes: %d. Minutos excedentes: %d. Valor base del viaje: %d.", travels, excessMinutes, totalTripValue);
        } else {
            // subscriptionType NONE u otro -> cobrar del balance sin importar si queda negativo
            Integer balance = user.getBalance() == null ? 0 : user.getBalance();
            Integer newBalance = balance - totalTripValue;
            user.setBalance(newBalance);

            subject = "Cobro de viaje por balance";
            message = String.format("Se cobró el viaje del balance. Valor cobrado: %d. Minutos excedentes: %d. Balance anterior: %d. Balance nuevo: %d.", totalTripValue, excessMinutes, balance, newBalance);
        }

        repository.save(user);

        // Publicar notificación del cobro
        try {
            UserDTO dto = new UserDTO();
            dto.setUserEmail(user.getUserEmail());
            dto.setSubject(subject);
            dto.setMessage(message);
            userPublisher.sendJsonMessage(dto);
        } catch (Exception e) {
            // No interrumpir la transacción por fallo de publicación; solo registrar
        }
    }
}
