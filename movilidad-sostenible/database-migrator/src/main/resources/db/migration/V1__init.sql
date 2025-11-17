/* Migración inicial trasladada desde viaje-service */
CREATE TABLE admins (
  k_admin_cc varchar(11) PRIMARY KEY,
  n_first_name varchar(50) NOT NULL,
  n_second_name varchar(50),
  n_first_lastname varchar(50) NOT NULL,
  n_second_lastname varchar(50),
  f_admin_birthday date NOT NULL,
  n_admin_email varchar(100) NOT NULL UNIQUE,
  f_admin_registration_date date NOT NULL
);
CREATE TABLE city (
  k_id_city serial PRIMARY KEY,
  n_city_name varchar(50) UNIQUE
);
CREATE TABLE station (
  k_id_station serial PRIMARY KEY,
  n_station_name varchar(50) NOT NULL,
  d_street varchar(40),
  d_avenue varchar(40),
  d_number varchar(40),
  k_id_city integer NOT NULL REFERENCES city(k_id_city)
);
CREATE INDEX IXFK_Station_City ON station(k_id_city);
CREATE TABLE bicycle (
  k_id_bicycle serial PRIMARY KEY,
  k_series integer NOT NULL UNIQUE,
  n_model varchar(50) NOT NULL,
  t_padlock_status varchar(50) NOT NULL DEFAULT 'CLOSE' CHECK (t_padlock_status IN ('CLOSE','OPEN')),
  f_last_update timestamp,
  n_latitude double precision,
  n_length double precision,
  v_battery numeric(5,2) CHECK (v_battery BETWEEN 0 AND 100),
  k_current_id_station integer NOT NULL REFERENCES station(k_id_station)
);
CREATE INDEX IXFK_bicycle_station ON bicycle(k_current_id_station);
CREATE TABLE users (
  k_user_cc integer PRIMARY KEY,
  n_username varchar(50) NOT NULL UNIQUE,
  n_hashed_password varchar(255) NOT NULL,
  n_user_first_name varchar(50) NOT NULL,
  n_user_second_name varchar(50),
  n_user_first_lastname varchar(50) NOT NULL,
  n_user_second_lastname varchar(50),
  f_user_birthdate date NOT NULL,
  n_user_email varchar(100) NOT NULL UNIQUE,
  t_subscription_type varchar(50) NOT NULL DEFAULT 'NONE' CHECK (t_subscription_type IN ('NONE','MONTHLY')),
  f_user_registration_date date NOT NULL,
  t_is_verified boolean NOT NULL DEFAULT false
);
CREATE TABLE email_verification (
  k_id_email_verification serial PRIMARY KEY,
  n_otp_hash varchar(255) NOT NULL,
  f_expires_at timestamp NOT NULL,
  t_consumed boolean NOT NULL DEFAULT false,
  f_created_at timestamp NOT NULL DEFAULT NOW(),
  k_user_cc integer NOT NULL REFERENCES users(k_user_cc) ON DELETE CASCADE,
  CONSTRAINT UNQ_email_ver_user UNIQUE (k_user_cc)
);
CREATE INDEX IX_email_ver_user ON email_verification(k_user_cc);
CREATE TABLE fine (
  k_id_fine serial PRIMARY KEY,
  v_amount numeric(9)
);
CREATE TABLE user_fine (
  k_user_fine serial PRIMARY KEY,
  n_reason varchar(250) NOT NULL,
  t_state varchar(50) NOT NULL DEFAULT 'PENDING' CHECK (t_state IN ('PENDING','PAID')),
  k_id_multa integer NOT NULL REFERENCES fine(k_id_fine),
  k_user_cc integer NOT NULL REFERENCES users(k_user_cc)
);
CREATE INDEX IX_user_fine_multa ON user_fine(k_id_multa);
CREATE INDEX IX_user_fine_user ON user_fine(k_user_cc);
CREATE TABLE payment_method (
  k_id_payment_method varchar(10) PRIMARY KEY,
  t_card_type varchar(10) NOT NULL,
  v_number varchar(16) NOT NULL,
  f_expiration_date date NOT NULL,
  n_owner_name varchar(50) NOT NULL,
  v_balance numeric(9),
  k_user_cc integer NOT NULL REFERENCES users(k_user_cc)
);
CREATE INDEX IX_payment_user ON payment_method(k_user_cc);
CREATE TABLE travel (
  k_id_travel serial PRIMARY KEY,
  f_started_at timestamp NOT NULL DEFAULT NOW(),
  f_ended_at timestamp,
  t_status varchar(20) NOT NULL CHECK (t_status IN ('REQUIRED','IN_PROGRESS','COMPLETED','CANCELLED')),
  k_user_cc integer NOT NULL REFERENCES users(k_user_cc),
  k_id_bicycle integer NOT NULL REFERENCES bicycle(k_id_bicycle),
  k_from_id_station integer NOT NULL REFERENCES station(k_id_station),
  k_to_id_station integer REFERENCES station(k_id_station)
);
CREATE INDEX IX_travel_bicycle ON travel(k_id_bicycle);
CREATE INDEX IX_travel_from_station ON travel(k_from_id_station);
CREATE INDEX IX_travel_to_station ON travel(k_to_id_station);
CREATE INDEX IX_travel_users ON travel(k_user_cc);

