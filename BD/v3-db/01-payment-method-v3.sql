-- v3 DB migration for payment_method
-- This script drops and recreates public.payment_method to match the payments microservice model.
-- WARNING: This will delete any existing data in payment_method.

DROP TABLE IF EXISTS public.payment_method CASCADE;

CREATE TABLE public.payment_method (
    k_id_payment_method BIGSERIAL PRIMARY KEY,
    k_user_cc           INTEGER      NOT NULL,
    t_card_type         VARCHAR(20)  NOT NULL,
    n_card_number       VARCHAR(20)  NOT NULL,
    n_card_number_full  VARCHAR(20),
    n_owner_name        VARCHAR(100) NOT NULL,
    f_expiration_date   DATE         NOT NULL,
    n_brand             VARCHAR(20),
    b_is_primary        BOOLEAN      DEFAULT FALSE,
    b_is_active         BOOLEAN      DEFAULT TRUE,
    f_registration_date TIMESTAMP    DEFAULT NOW(),
    n_billing_address   VARCHAR(255),
    n_postal_code       VARCHAR(20),
    v_balance           BIGINT       DEFAULT 0
);

-- Foreign key to users
ALTER TABLE public.payment_method
    ADD CONSTRAINT fk_payment_method_user
    FOREIGN KEY (k_user_cc) REFERENCES public.users (k_user_cc)
    ON UPDATE NO ACTION ON DELETE NO ACTION;

-- Basic index by user
CREATE INDEX ix_payment_method_user
    ON public.payment_method (k_user_cc);

-- Grant privileges to manager_pay_method role (created in roles scripts)
GRANT SELECT, INSERT, UPDATE, DELETE ON public.payment_method TO manager_pay_method;
