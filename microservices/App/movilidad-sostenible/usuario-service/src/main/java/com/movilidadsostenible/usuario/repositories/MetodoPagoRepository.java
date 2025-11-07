package com.movilidadsostenible.usuario.repositories;

import com.movilidadsostenible.usuario.models.entity.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {

    // Buscar todos los métodos de pago de un usuario
    List<MetodoPago> findByK_usuarioCCAndB_activoTrue(String k_usuarioCC);

    // Buscar todos los métodos de pago de un usuario (incluyendo inactivos)
    List<MetodoPago> findByK_usuarioCC(String k_usuarioCC);

    // Buscar el método de pago principal de un usuario
    Optional<MetodoPago> findByK_usuarioCCAndB_principalTrueAndB_activoTrue(String k_usuarioCC);

    // Buscar un método de pago específico de un usuario
    Optional<MetodoPago> findByK_metodoPagoAndK_usuarioCC(Long k_metodoPago, String k_usuarioCC);

    // Actualizar todos los métodos de pago de un usuario para que no sean principales
    @Modifying
    @Query("UPDATE MetodoPago m SET m.b_principal = false WHERE m.k_usuarioCC = :k_usuarioCC")
    void desmarcarTodosPrincipales(@Param("k_usuarioCC") String k_usuarioCC);

    // Contar métodos de pago activos de un usuario
    Long countByK_usuarioCCAndB_activoTrue(String k_usuarioCC);

    // Buscar por tipo de tarjeta
    List<MetodoPago> findByK_usuarioCCAndT_tipoTarjetaAndB_activoTrue(
        String k_usuarioCC, String t_tipoTarjeta);
}
