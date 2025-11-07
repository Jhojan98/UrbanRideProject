package com.movilidadsostenible.usuario.controllers;

import com.movilidadsostenible.usuario.models.dto.MetodoPagoDTO;
import com.movilidadsostenible.usuario.models.entity.MetodoPago;
import com.movilidadsostenible.usuario.services.MetodoPagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/metodos-pago")
@CrossOrigin(origins = "*")
public class MetodoPagoController {

    @Autowired
    private MetodoPagoService service;

    /**
     * Listar todos los métodos de pago de un usuario
     * GET /api/metodos-pago/usuario/{usuarioCC}
     */
    @GetMapping("/usuario/{usuarioCC}")
    public ResponseEntity<List<MetodoPagoDTO>> listarMetodosPagoUsuario(
            @PathVariable String usuarioCC) {
        try {
            List<MetodoPagoDTO> metodosPago = service.listarMetodosPagoUsuario(usuarioCC);
            return ResponseEntity.ok(metodosPago);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener un método de pago específico
     * GET /api/metodos-pago/{id}/usuario/{usuarioCC}
     */
    @GetMapping("/{id}/usuario/{usuarioCC}")
    public ResponseEntity<MetodoPagoDTO> obtenerMetodoPago(
            @PathVariable Long id,
            @PathVariable String usuarioCC) {
        try {
            Optional<MetodoPagoDTO> metodoPago = service.obtenerMetodoPagoPorId(id, usuarioCC);
            return metodoPago.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener el método de pago principal de un usuario
     * GET /api/metodos-pago/usuario/{usuarioCC}/principal
     */
    @GetMapping("/usuario/{usuarioCC}/principal")
    public ResponseEntity<MetodoPagoDTO> obtenerMetodoPagoPrincipal(
            @PathVariable String usuarioCC) {
        try {
            Optional<MetodoPagoDTO> metodoPago = service.obtenerMetodoPagoPrincipal(usuarioCC);
            return metodoPago.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Agregar un nuevo método de pago
     * POST /api/metodos-pago
     */
    @PostMapping
    public ResponseEntity<?> agregarMetodoPago(@RequestBody MetodoPago metodoPago) {
        try {
            MetodoPagoDTO nuevoMetodoPago = service.agregarMetodoPago(metodoPago);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoMetodoPago);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al agregar el método de pago: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Actualizar un método de pago
     * PUT /api/metodos-pago/{id}/usuario/{usuarioCC}
     */
    @PutMapping("/{id}/usuario/{usuarioCC}")
    public ResponseEntity<?> actualizarMetodoPago(
            @PathVariable Long id,
            @PathVariable String usuarioCC,
            @RequestBody MetodoPago metodoPago) {
        try {
            MetodoPagoDTO actualizado = service.actualizarMetodoPago(id, usuarioCC, metodoPago);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar el método de pago");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Eliminar (desactivar) un método de pago
     * DELETE /api/metodos-pago/{id}/usuario/{usuarioCC}
     */
    @DeleteMapping("/{id}/usuario/{usuarioCC}")
    public ResponseEntity<?> eliminarMetodoPago(
            @PathVariable Long id,
            @PathVariable String usuarioCC) {
        try {
            service.eliminarMetodoPago(id, usuarioCC);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Método de pago eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar el método de pago");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Establecer un método de pago como principal
     * PATCH /api/metodos-pago/{id}/usuario/{usuarioCC}/principal
     */
    @PatchMapping("/{id}/usuario/{usuarioCC}/principal")
    public ResponseEntity<?> establecerComoPrincipal(
            @PathVariable Long id,
            @PathVariable String usuarioCC) {
        try {
            MetodoPagoDTO metodoPago = service.establecerComoPrincipal(id, usuarioCC);
            return ResponseEntity.ok(metodoPago);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al establecer método de pago principal");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Validar un número de tarjeta
     * POST /api/metodos-pago/validar
     */
    @PostMapping("/validar")
    public ResponseEntity<Map<String, Boolean>> validarNumeroTarjeta(
            @RequestBody Map<String, String> request) {
        try {
            String numeroTarjeta = request.get("numeroTarjeta");
            boolean esValido = service.validarNumeroTarjeta(numeroTarjeta);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("valido", esValido);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Boolean> response = new HashMap<>();
            response.put("valido", false);
            return ResponseEntity.ok(response);
        }
    }
}
