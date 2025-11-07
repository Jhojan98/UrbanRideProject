-- Script de inicialización para la tabla metodo_pago
-- Compatible con la entidad MetodoPago de Spring Boot

-- Eliminar la tabla antigua si existe (con una estructura diferente)
DROP TABLE IF EXISTS "MetodoPago" CASCADE;

-- Crear la nueva tabla metodo_pago con la estructura actualizada
CREATE TABLE IF NOT EXISTS metodo_pago (
    k_metodoPago BIGSERIAL PRIMARY KEY,
    k_usuario_cc VARCHAR(50) NOT NULL,
    t_tipoTarjeta VARCHAR(20) NOT NULL,
    n_numeroTarjeta VARCHAR(20) NOT NULL,
    n_numeroTarjetaCompleto VARCHAR(20),
    n_nombreTitular VARCHAR(100) NOT NULL,
    f_fechaExpiracion DATE NOT NULL,
    n_marca VARCHAR(20),
    b_principal BOOLEAN DEFAULT FALSE,
    b_activo BOOLEAN DEFAULT TRUE,
    f_fechaRegistro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    n_direccionFacturacion VARCHAR(255),
    n_codigoPostal VARCHAR(20),
    CONSTRAINT fk_usuario FOREIGN KEY (k_usuario_cc) REFERENCES usuario(k_CC) ON DELETE CASCADE
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_metodo_pago_usuario ON metodo_pago(k_usuario_cc);
CREATE INDEX idx_metodo_pago_principal ON metodo_pago(k_usuario_cc, b_principal) WHERE b_principal = TRUE;
CREATE INDEX idx_metodo_pago_activo ON metodo_pago(k_usuario_cc, b_activo) WHERE b_activo = TRUE;

-- Comentarios en las columnas
COMMENT ON TABLE metodo_pago IS 'Tabla que almacena los métodos de pago de los usuarios';
COMMENT ON COLUMN metodo_pago.k_metodoPago IS 'ID único del método de pago';
COMMENT ON COLUMN metodo_pago.k_usuario_cc IS 'Cédula del usuario propietario';
COMMENT ON COLUMN metodo_pago.t_tipoTarjeta IS 'Tipo de tarjeta: CREDITO, DEBITO, PSE, EFECTIVO';
COMMENT ON COLUMN metodo_pago.n_numeroTarjeta IS 'Número de tarjeta enmascarado (últimos 4 dígitos)';
COMMENT ON COLUMN metodo_pago.n_numeroTarjetaCompleto IS 'Número completo de tarjeta (debe ser encriptado en producción)';
COMMENT ON COLUMN metodo_pago.n_nombreTitular IS 'Nombre del titular de la tarjeta';
COMMENT ON COLUMN metodo_pago.f_fechaExpiracion IS 'Fecha de expiración de la tarjeta';
COMMENT ON COLUMN metodo_pago.n_marca IS 'Marca de la tarjeta: VISA, MASTERCARD, AMEX, etc.';
COMMENT ON COLUMN metodo_pago.b_principal IS 'Indica si es el método de pago principal del usuario';
COMMENT ON COLUMN metodo_pago.b_activo IS 'Indica si el método de pago está activo';
COMMENT ON COLUMN metodo_pago.f_fechaRegistro IS 'Fecha de registro del método de pago';
COMMENT ON COLUMN metodo_pago.n_direccionFacturacion IS 'Dirección de facturación asociada';
COMMENT ON COLUMN metodo_pago.n_codigoPostal IS 'Código postal de facturación';

-- Datos de ejemplo para pruebas (SOLO PARA DESARROLLO)
-- NOTA: En producción, NUNCA almacenes números de tarjeta reales sin encriptación
INSERT INTO metodo_pago (
    k_usuario_cc, 
    t_tipoTarjeta, 
    n_numeroTarjeta, 
    n_numeroTarjetaCompleto,
    n_nombreTitular, 
    f_fechaExpiracion, 
    n_marca, 
    b_principal,
    n_direccionFacturacion,
    n_codigoPostal
) VALUES 
-- Estos son números de tarjeta de prueba válidos según el algoritmo de Luhn
('1234567890', 'CREDITO', '**** **** **** 1111', '4532015112830366', 'JUAN PEREZ', '2026-12-31', 'VISA', TRUE, 'Calle 123 #45-67', '110111'),
('1234567890', 'DEBITO', '**** **** **** 8431', '5425233430109903', 'JUAN PEREZ', '2025-06-30', 'MASTERCARD', FALSE, 'Calle 123 #45-67', '110111')
ON CONFLICT DO NOTHING;

-- Constraint para asegurar que solo haya un método principal activo por usuario
CREATE OR REPLACE FUNCTION check_single_primary_payment() 
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.b_principal = TRUE AND NEW.b_activo = TRUE THEN
        UPDATE metodo_pago 
        SET b_principal = FALSE 
        WHERE k_usuario_cc = NEW.k_usuario_cc 
          AND k_metodoPago != NEW.k_metodoPago 
          AND b_principal = TRUE;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_single_primary_payment
    BEFORE INSERT OR UPDATE ON metodo_pago
    FOR EACH ROW
    EXECUTE FUNCTION check_single_primary_payment();
