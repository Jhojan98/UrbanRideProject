package com.movilidadsostenible.usuario.services;

import com.movilidadsostenible.usuario.models.dto.MetodoPagoDTO;
import com.movilidadsostenible.usuario.models.entity.MetodoPago;

import java.util.List;
import java.util.Optional;

public interface MetodoPagoService {

    // Crear un nuevo método de pago
    MetodoPagoDTO agregarMetodoPago(MetodoPago metodoPago);

    // Listar todos los métodos de pago de un usuario (activos)
    List<MetodoPagoDTO> listarMetodosPagoUsuario(String k_usuarioCC);

    // Obtener un método de pago específico
    Optional<MetodoPagoDTO> obtenerMetodoPagoPorId(Long k_metodoPago, String k_usuarioCC);

    // Actualizar un método de pago
    MetodoPagoDTO actualizarMetodoPago(Long k_metodoPago, String k_usuarioCC, MetodoPago metodoPago);

    // Eliminar (desactivar) un método de pago
    void eliminarMetodoPago(Long k_metodoPago, String k_usuarioCC);

    // Establecer un método de pago como principal
    MetodoPagoDTO establecerComoPrincipal(Long k_metodoPago, String k_usuarioCC);

    // Obtener el método de pago principal de un usuario
    Optional<MetodoPagoDTO> obtenerMetodoPagoPrincipal(String k_usuarioCC);

    // Validar si un método de pago existe y pertenece al usuario
    boolean validarPropietario(Long k_metodoPago, String k_usuarioCC);

    // Validar número de tarjeta (Algoritmo de Luhn)
    boolean validarNumeroTarjeta(String numeroTarjeta);
}
