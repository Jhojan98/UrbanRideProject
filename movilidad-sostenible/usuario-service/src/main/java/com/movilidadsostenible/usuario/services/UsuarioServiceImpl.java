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
            user.setBalance(0.0);
        }
        return repository.save(user);
    }

    @Override
    @Transactional
    public void delete(String uid) {
        repository.deleteById(uid);
    }

    // Implementación exigida por la interfaz (Integer)
    @Override
    public Double getBalance(String uidUser) {
        Double bal = byId(uidUser).map(User::getBalance).orElse(null);
        return bal;
    }

    // Versión con Double para uso interno (no override)
    public Double getBalanceDouble(String uidUser) {
        return byId(uidUser).map(User::getBalance).orElse(null);
    }

    // Implementación exigida por la interfaz (Integer amount)
    @Override
    @Transactional
    public Double addBalance(String uidUser, Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("El monto debe ser positivo");
        }
        User user = byId(uidUser).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Double current = user.getBalance() == null ? 0.0 : user.getBalance();
        user.setBalance(current + amount);
        repository.save(user);
        return user.getBalance(); // retornar Double
    }

    // Versión con Double para uso interno (no override)
    @Transactional
    @Override
    public Double subtractBalance(String uidUser, Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("El monto debe ser positivo");
        }
        User user = byId(uidUser).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Double current = user.getBalance() == null ? 0.0 : user.getBalance();
        if (current < amount) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }
        user.setBalance(current - amount);
        repository.save(user);
        return user.getBalance(); // retornar Double
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
            Integer travelsAvailable = user.getSubscriptionTravels();
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

        Double balance = user.getBalance();
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
    public void chargeTravel(Double totalTripValue, Integer excessMinutes, String uidUser) {
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

        try {
            UserDTO dto = new UserDTO();
            dto.setUserEmail(user.getUserEmail());

            if ("MONTHLY".equalsIgnoreCase(subscriptionType) && user.getSubscriptionTravels() != null && user.getSubscriptionTravels() > 0) {
                Integer travels = user.getSubscriptionTravels();
                if (travels == null) travels = 0;
                // Resta 1 por el viaje incluido
                travels -= 1;
                // Si hay minutos excedentes, resta 1 adicional
                if (excessMinutes > 0) {
                    travels -= 1;
                }
                user.setSubscriptionTravels(travels);

                subject = "Cobro de viaje por suscripción MONTHLY";
                message = String.format(
                        "Se cobró un viaje usando la suscripción mensual. Viajes restantes: %d. Minutos excedentes: %d. Valor base del viaje: %.2f.",
                        travels, excessMinutes, totalTripValue);

                dto.setSubject(subject);
                dto.setMessage(message);

                userPublisher.sendJsonhChargeTravelSubscriptionMessage(dto);

            } else {
                // subscriptionType NONE u otro -> cobrar del balance sin importar si queda negativo
                Double balance = user.getBalance() == null ? 0.0 : user.getBalance();
                Double newBalance = balance - totalTripValue;
                user.setBalance(newBalance);

                subject = "Cobro de viaje por balance";
                message = String.format(
                        "Se cobró el viaje del balance. Valor cobrado: %.2f. Minutos excedentes: %d. Balance anterior: %.2f. Balance nuevo: %.2f.",
                        totalTripValue, excessMinutes, balance, newBalance);

                dto.setSubject(subject);
                dto.setMessage(message);

                userPublisher.sendJsonhChargeTravelBalanceMessage(dto);

            }
        } catch (Exception e) {
          // No interrumpir la transacción por fallo de publicación; solo registrar
        }

        repository.save(user);
    }

    @Override
    @Transactional
    public User purchaseMonthlySubscription(String uidUser) {
        User user = byId(uidUser).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Cambiar tipo de suscripción a MONTHLY
        user.setSubscriptionType("MONTHLY");

        // Incrementar viajes de suscripción, inicializar a 150 si es null
        Integer subscriptionTravels = user.getSubscriptionTravels();
        if (subscriptionTravels == null) {
            subscriptionTravels = 150;
        } else {
            subscriptionTravels += 150;
        }
        user.setSubscriptionTravels(subscriptionTravels);

        // Restar 39 del balance, inicializar a 0 si es null
        Double balance = user.getBalance() == null ? 0.0 : user.getBalance();

        // Si no hay saldo suficiente, lanzar excepción
        if (balance < 39.0) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }

        balance -= 39.0;
        user.setBalance(balance);

        // Guardar cambios
        return repository.save(user);
    }
}
