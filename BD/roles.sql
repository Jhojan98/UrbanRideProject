-- Roles principales 
CREATE ROLE manager_users LOGIN PASSWORD 'g_user'; 
CREATE ROLE manager_bicycle LOGIN PASSWORD 'g_bicy'; 
CREATE ROLE manager_travel LOGIN PASSWORD 'g_travel'; 
CREATE ROLE manager_admins LOGIN PASSWORD 'g_admin';
CREATE ROLE manager_pay_method LOGIN PASSWORD 'g_pay_meth';
CREATE ROLE manager_fine LOGIN PASSWORD 'g_fine';
CREATE ROLE manager_user_fine LOGIN PASSWORD 'g_user_fine';
CREATE ROLE manager_starts_in LOGIN PASSWORD 'g_starts';
CREATE ROLE manager_ends_in LOGIN PASSWORD 'g_ends';
CREATE ROLE manager_station LOGIN PASSWORD 'g_station';
CREATE ROLE manager_city LOGIN PASSWORD 'g_city';

-- Privilegios sobre tablas
GRANT SELECT, INSERT, UPDATE, DELETE ON public.users TO manager_users;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.bicycle TO manager_bicycle;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.admins TO manager_admins;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.city  TO manager_city;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.starts_in  TO manager_starts_in;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.ends_in TO manager_ends_in;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.estation TO manager_station;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.fine TO manager_fine;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.payment_method TO manager_pay_method;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.travel TO manager_travel;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.user_fine TO manager_user_fine;
