-- ============================================================================
-- UPGRADE: Payment Method Table for Movilidad Sostenible
-- Replaces simple payment_method with full featured version
-- Schema: public (no separate schemas)
-- Compatible with existing roles: manager_pay_method
-- ============================================================================

-- Drop existing payment_method table and constraints
DROP TABLE IF EXISTS public.payment_method CASCADE;

-- Create enhanced payment_method table
CREATE TABLE public.payment_method (
    k_id_payment_method BIGSERIAL PRIMARY KEY,
    k_user_cc INTEGER NOT NULL,
    t_card_type VARCHAR(20) NOT NULL,
    n_card_number VARCHAR(20) NOT NULL,
    n_card_number_full VARCHAR(20),
    n_owner_name VARCHAR(100) NOT NULL,
    f_expiration_date DATE NOT NULL,
    n_brand VARCHAR(20),
    b_is_primary BOOLEAN DEFAULT FALSE,
    b_is_active BOOLEAN DEFAULT TRUE,
    f_registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    n_billing_address VARCHAR(255),
    n_postal_code VARCHAR(20),
    v_balance BIGINT DEFAULT 0,
    
    -- Foreign key to users table
    CONSTRAINT FK_Payment_method_User 
        FOREIGN KEY (k_user_cc) 
        REFERENCES public.users (k_user_cc) 
        ON DELETE CASCADE 
        ON UPDATE NO ACTION
);

-- Create indexes for better performance
CREATE INDEX idx_payment_method_user ON public.payment_method(k_user_cc);
CREATE INDEX idx_payment_method_primary ON public.payment_method(k_user_cc, b_is_primary) WHERE b_is_primary = TRUE;
CREATE INDEX idx_payment_method_active ON public.payment_method(k_user_cc, b_is_active) WHERE b_is_active = TRUE;
CREATE INDEX idx_payment_method_brand ON public.payment_method(n_brand);

-- Add constraints
ALTER TABLE public.payment_method 
    ADD CONSTRAINT CHK_t_card_type 
    CHECK (t_card_type IN ('CREDITO', 'DEBITO', 'PSE', 'EFECTIVO'));

ALTER TABLE public.payment_method 
    ADD CONSTRAINT CHK_v_balance 
    CHECK (v_balance >= 0);

-- Add table and column comments
COMMENT ON TABLE public.payment_method IS 'Payment methods (cards) associated with users - Enhanced version';
COMMENT ON COLUMN public.payment_method.k_id_payment_method IS 'Unique payment method ID (auto-increment)';
COMMENT ON COLUMN public.payment_method.k_user_cc IS 'User ID (cedula) - foreign key to users table';
COMMENT ON COLUMN public.payment_method.t_card_type IS 'Card type: CREDITO, DEBITO, PSE, EFECTIVO';
COMMENT ON COLUMN public.payment_method.n_card_number IS 'Masked card number (**** **** **** 1234)';
COMMENT ON COLUMN public.payment_method.n_card_number_full IS 'Full card number (encrypted in production)';
COMMENT ON COLUMN public.payment_method.n_owner_name IS 'Cardholder name';
COMMENT ON COLUMN public.payment_method.f_expiration_date IS 'Card expiration date';
COMMENT ON COLUMN public.payment_method.n_brand IS 'Card brand: VISA, MASTERCARD, AMEX, DISCOVER, etc.';
COMMENT ON COLUMN public.payment_method.b_is_primary IS 'Is this the primary payment method for the user?';
COMMENT ON COLUMN public.payment_method.b_is_active IS 'Is this payment method active? (soft delete)';
COMMENT ON COLUMN public.payment_method.f_registration_date IS 'Registration timestamp';
COMMENT ON COLUMN public.payment_method.n_billing_address IS 'Billing address';
COMMENT ON COLUMN public.payment_method.n_postal_code IS 'Postal code';
COMMENT ON COLUMN public.payment_method.v_balance IS 'Available balance for this payment method (in cents)';

-- ============================================================================
-- Function: Ensure only one primary payment method per user
-- ============================================================================
CREATE OR REPLACE FUNCTION public.ensure_single_primary_payment()
RETURNS TRIGGER AS $$
BEGIN
    -- If setting a payment method as primary, unset all others for this user
    IF NEW.b_is_primary = TRUE THEN
        UPDATE public.payment_method 
        SET b_is_primary = FALSE 
        WHERE k_user_cc = NEW.k_user_cc 
          AND k_id_payment_method != NEW.k_id_payment_method 
          AND b_is_primary = TRUE;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- Trigger: Enforce single primary payment method
-- ============================================================================
DROP TRIGGER IF EXISTS trigger_single_primary_payment ON public.payment_method;
CREATE TRIGGER trigger_single_primary_payment
    BEFORE INSERT OR UPDATE ON public.payment_method
    FOR EACH ROW
    EXECUTE FUNCTION public.ensure_single_primary_payment();

-- ============================================================================
-- Grant permissions to existing roles
-- ============================================================================
-- Grant all operations to manager_pay_method role
GRANT SELECT, INSERT, UPDATE, DELETE ON public.payment_method TO manager_pay_method;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE payment_method_k_id_payment_method_seq TO manager_pay_method;

-- Grant read access to manager_users (may need to see payment info)
GRANT SELECT ON public.payment_method TO manager_users;

-- ============================================================================
-- Sample data (optional - for testing)
-- ============================================================================
-- Uncomment to insert test data
/*
INSERT INTO public.payment_method (k_user_cc, t_card_type, n_card_number, n_owner_name, f_expiration_date, n_brand, b_is_primary, v_balance)
VALUES 
    (1, 'CREDITO', '**** **** **** 1234', 'Juan Perez', '2025-12-31', 'VISA', TRUE, 50000),
    (1, 'DEBITO', '**** **** **** 5678', 'Juan Perez', '2026-06-30', 'MASTERCARD', FALSE, 25000);
*/

-- Success message
SELECT 'Payment method table upgraded successfully in public schema' AS status;
