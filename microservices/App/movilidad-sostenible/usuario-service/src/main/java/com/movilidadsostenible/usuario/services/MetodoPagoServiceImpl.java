package com.movilidadsostenible.usuario.services;

import com.movilidadsostenible.usuario.models.dto.MetodoPagoDTO;
import com.movilidadsostenible.usuario.models.entity.MetodoPago;
import com.movilidadsostenible.usuario.repositories.MetodoPagoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MetodoPagoServiceImpl implements MetodoPagoService {

    @Autowired
    private MetodoPagoRepository repository;

    @Override
    @Transactional
    public MetodoPagoDTO agregarMetodoPago(MetodoPago metodoPago) {
        // Validar número de tarjeta
        if (!validarNumeroTarjeta(metodoPago.getN_numeroTarjetaCompleto())) {
            throw new IllegalArgumentException("Número de tarjeta inválido");
        }

        // Validar fecha de expiración
        if (metodoPago.getF_fechaExpiracion().before(new Date())) {
            throw new IllegalArgumentException("La tarjeta está vencida");
        }

        // Detectar marca de la tarjeta
        String marca = metodoPago.detectarMarca();
        metodoPago.setN_marca(marca);

        // Generar número enmascarado para almacenar
        metodoPago.setN_numeroTarjeta(metodoPago.getMaskedCardNumber());

        // Establecer fecha de registro
        metodoPago.setF_fechaRegistro(new Date());

        // Si es el primer método de pago del usuario, hacerlo principal
        Long cantidadMetodos = repository.countByK_usuarioCCAndB_activoTrue(
            metodoPago.getK_usuarioCC());
        
        if (cantidadMetodos == 0) {
            metodoPago.setB_principal(true);
        } else if (metodoPago.getB_principal() != null && metodoPago.getB_principal()) {
            // Si se marca como principal, desmarcar los demás
            repository.desmarcarTodosPrincipales(metodoPago.getK_usuarioCC());
        }

        MetodoPago guardado = repository.save(metodoPago);
        return new MetodoPagoDTO(guardado);
    }

    @Override
    public List<MetodoPagoDTO> listarMetodosPagoUsuario(String k_usuarioCC) {
        List<MetodoPago> metodosPago = repository.findByK_usuarioCCAndB_activoTrue(k_usuarioCC);
        return metodosPago.stream()
                .map(MetodoPagoDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MetodoPagoDTO> obtenerMetodoPagoPorId(Long k_metodoPago, String k_usuarioCC) {
        Optional<MetodoPago> metodoPago = repository.findByK_metodoPagoAndK_usuarioCC(
            k_metodoPago, k_usuarioCC);
        return metodoPago.map(MetodoPagoDTO::new);
    }

    @Override
    @Transactional
    public MetodoPagoDTO actualizarMetodoPago(Long k_metodoPago, String k_usuarioCC, 
                                               MetodoPago metodoPagoActualizado) {
        Optional<MetodoPago> metodoPagoOptional = repository.findByK_metodoPagoAndK_usuarioCC(
            k_metodoPago, k_usuarioCC);

        if (metodoPagoOptional.isEmpty()) {
            throw new IllegalArgumentException("Método de pago no encontrado");
        }

        MetodoPago metodoPagoDB = metodoPagoOptional.get();

        // Actualizar solo campos permitidos (no se actualiza el número de tarjeta por seguridad)
        if (metodoPagoActualizado.getN_nombreTitular() != null) {
            metodoPagoDB.setN_nombreTitular(metodoPagoActualizado.getN_nombreTitular());
        }

        if (metodoPagoActualizado.getF_fechaExpiracion() != null) {
            if (metodoPagoActualizado.getF_fechaExpiracion().before(new Date())) {
                throw new IllegalArgumentException("La tarjeta está vencida");
            }
            metodoPagoDB.setF_fechaExpiracion(metodoPagoActualizado.getF_fechaExpiracion());
        }

        if (metodoPagoActualizado.getN_direccionFacturacion() != null) {
            metodoPagoDB.setN_direccionFacturacion(metodoPagoActualizado.getN_direccionFacturacion());
        }

        if (metodoPagoActualizado.getN_codigoPostal() != null) {
            metodoPagoDB.setN_codigoPostal(metodoPagoActualizado.getN_codigoPostal());
        }

        MetodoPago actualizado = repository.save(metodoPagoDB);
        return new MetodoPagoDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminarMetodoPago(Long k_metodoPago, String k_usuarioCC) {
        Optional<MetodoPago> metodoPagoOptional = repository.findByK_metodoPagoAndK_usuarioCC(
            k_metodoPago, k_usuarioCC);

        if (metodoPagoOptional.isEmpty()) {
            throw new IllegalArgumentException("Método de pago no encontrado");
        }

        MetodoPago metodoPago = metodoPagoOptional.get();
        
        // Si era el método principal, desactivarlo y asignar otro como principal
        if (metodoPago.getB_principal()) {
            metodoPago.setB_activo(false);
            metodoPago.setB_principal(false);
            repository.save(metodoPago);

            // Buscar otro método de pago activo para hacerlo principal
            List<MetodoPago> otrosMetodos = repository.findByK_usuarioCCAndB_activoTrue(k_usuarioCC);
            if (!otrosMetodos.isEmpty()) {
                MetodoPago nuevoPrincipal = otrosMetodos.get(0);
                nuevoPrincipal.setB_principal(true);
                repository.save(nuevoPrincipal);
            }
        } else {
            metodoPago.setB_activo(false);
            repository.save(metodoPago);
        }
    }

    @Override
    @Transactional
    public MetodoPagoDTO establecerComoPrincipal(Long k_metodoPago, String k_usuarioCC) {
        Optional<MetodoPago> metodoPagoOptional = repository.findByK_metodoPagoAndK_usuarioCC(
            k_metodoPago, k_usuarioCC);

        if (metodoPagoOptional.isEmpty()) {
            throw new IllegalArgumentException("Método de pago no encontrado");
        }

        MetodoPago metodoPago = metodoPagoOptional.get();

        if (!metodoPago.getB_activo()) {
            throw new IllegalArgumentException("No se puede establecer como principal un método de pago inactivo");
        }

        // Desmarcar todos los métodos como principales
        repository.desmarcarTodosPrincipales(k_usuarioCC);

        // Establecer este como principal
        metodoPago.setB_principal(true);
        MetodoPago actualizado = repository.save(metodoPago);

        return new MetodoPagoDTO(actualizado);
    }

    @Override
    public Optional<MetodoPagoDTO> obtenerMetodoPagoPrincipal(String k_usuarioCC) {
        Optional<MetodoPago> metodoPago = repository
            .findByK_usuarioCCAndB_principalTrueAndB_activoTrue(k_usuarioCC);
        return metodoPago.map(MetodoPagoDTO::new);
    }

    @Override
    public boolean validarPropietario(Long k_metodoPago, String k_usuarioCC) {
        return repository.findByK_metodoPagoAndK_usuarioCC(k_metodoPago, k_usuarioCC).isPresent();
    }

    @Override
    public boolean validarNumeroTarjeta(String numeroTarjeta) {
        if (numeroTarjeta == null || numeroTarjeta.isEmpty()) {
            return false;
        }

        // Eliminar espacios y guiones
        numeroTarjeta = numeroTarjeta.replaceAll("[\\s-]", "");

        // Verificar que solo contenga dígitos
        if (!numeroTarjeta.matches("\\d+")) {
            return false;
        }

        // Verificar longitud (entre 13 y 19 dígitos)
        if (numeroTarjeta.length() < 13 || numeroTarjeta.length() > 19) {
            return false;
        }

        // Algoritmo de Luhn
        int sum = 0;
        boolean alternate = false;

        for (int i = numeroTarjeta.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(numeroTarjeta.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }
}
