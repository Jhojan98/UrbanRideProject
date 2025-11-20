-- v3 DB migration for Stripe Customer mapping
-- This script creates the public.stripe_customer table used by the payment-service
-- to map internal users (k_user_cc) to Stripe Customer IDs.

CREATE TABLE IF NOT EXISTS public.stripe_customer (
    k_user_cc          INTEGER      PRIMARY KEY,
    stripe_customer_id VARCHAR(255) NOT NULL,
    f_creacion         TIMESTAMP    DEFAULT NOW()
);

ALTER TABLE public.stripe_customer
    ADD CONSTRAINT fk_stripe_customer_user
    FOREIGN KEY (k_user_cc) REFERENCES public.users (k_user_cc)
    ON UPDATE NO ACTION ON DELETE NO ACTION;

-- Grant privileges so manager_pay_method (used by payment-service) can access this table
GRANT SELECT, INSERT, UPDATE, DELETE ON public.stripe_customer TO manager_pay_method;
