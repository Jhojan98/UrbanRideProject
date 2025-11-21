-- v3 DB privileges for payment_method sequence
-- Grant usage on the sequence created by BIGSERIAL PK

GRANT USAGE, SELECT ON SEQUENCE public.payment_method_k_id_payment_method_seq
    TO manager_pay_method;
