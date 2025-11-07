-- Script simplificado para crear tabla metodo_pago
-- Sin dependencias de otras tablas

-- Eliminar la tabla si existe
DROP TABLE IF EXISTS metodo_pago CASCADE;

-- Crear la tabla metodo_pago con todos los campos
CREATE TABLE metodo_pago (
    k_metodopago BIGSERIAL PRIMARY KEY,
    k_usuario_cc VARCHAR(50) NOT NULL,
    t_tipotarjeta VARCHAR(20) NOT NULL,
    n_numerotarjeta VARCHAR(20) NOT NULL,
    n_numerotarjetacompleto VARCHAR(20),
    n_nombretitular VARCHAR(100) NOT NULL,
    f_fechaexpiracion DATE NOT NULL,
    n_marca VARCHAR(20),
    b_principal BOOLEAN DEFAULT FALSE,
    b_activo BOOLEAN DEFAULT TRUE,
    f_fecharegistro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    n_direccionfacturacion VARCHAR(255),
    n_codigopostal VARCHAR(20),
    v_saldo BIGINT DEFAULT 0
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_metodo_pago_usuario ON metodo_pago(k_usuario_cc);
CREATE INDEX idx_metodo_pago_principal ON metodo_pago(k_usuario_cc, b_principal) WHERE b_principal = TRUE;
CREATE INDEX idx_metodo_pago_activo ON metodo_pago(k_usuario_cc, b_activo) WHERE b_activo = TRUE;

-- Comentarios
COMMENT ON TABLE metodo_pago IS 'Tabla que almacena los métodos de pago de los usuarios';
COMMENT ON COLUMN metodo_pago.k_metodopago IS 'ID único del método de pago';
COMMENT ON COLUMN metodo_pago.k_usuario_cc IS 'Cédula del usuario propietario';
COMMENT ON COLUMN metodo_pago.t_tipotarjeta IS 'Tipo de tarjeta: CREDITO, DEBITO, PSE, EFECTIVO';
COMMENT ON COLUMN metodo_pago.n_numerotarjeta IS 'Número de tarjeta enmascarado';
COMMENT ON COLUMN metodo_pago.n_numerotarjetacompleto IS 'Número completo de tarjeta';
COMMENT ON COLUMN metodo_pago.n_nombretitular IS 'Nombre del titular';
COMMENT ON COLUMN metodo_pago.f_fechaexpiracion IS 'Fecha de expiración';
COMMENT ON COLUMN metodo_pago.n_marca IS 'Marca de la tarjeta';
COMMENT ON COLUMN metodo_pago.b_principal IS 'Indica si es el método principal';
COMMENT ON COLUMN metodo_pago.b_activo IS 'Indica si está activo';
COMMENT ON COLUMN metodo_pago.f_fecharegistro IS 'Fecha de registro';
COMMENT ON COLUMN metodo_pago.n_direccionfacturacion IS 'Dirección de facturación';
COMMENT ON COLUMN metodo_pago.n_codigopostal IS 'Código postal';
COMMENT ON COLUMN metodo_pago.v_saldo IS 'Saldo disponible en el método de pago';

-- Función para asegurar un solo método principal por usuario
CREATE OR REPLACE FUNCTION asegurar_un_principal()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.b_principal = TRUE THEN
        UPDATE metodo_pago 
        SET b_principal = FALSE 
        WHERE k_usuario_cc = NEW.k_usuario_cc 
          AND k_metodopago != NEW.k_metodopago 
          AND b_principal = TRUE;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para asegurar un solo método principal
DROP TRIGGER IF EXISTS trigger_un_principal ON metodo_pago;
CREATE TRIGGER trigger_un_principal
    BEFORE INSERT OR UPDATE ON metodo_pago
    FOR EACH ROW
    EXECUTE FUNCTION asegurar_un_principal();

-- Mensaje de confirmación
SELECT 'Tabla metodo_pago creada exitosamente con campo v_saldo' AS status;
