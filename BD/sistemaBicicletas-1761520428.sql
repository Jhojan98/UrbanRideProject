CREATE TABLE IF NOT EXISTS "Administrador" (
	"k_CC" serial NOT NULL UNIQUE,
	"n_primerNombre" varchar(50) NOT NULL,
	"n_segundoNombre" varchar(50),
	"n_primerApellido" varchar(50) NOT NULL,
	"n_segundoApellido" varchar(50),
	"f_fechaNacimiento" date NOT NULL,
	"n_correoElectronico" varchar(100) NOT NULL UNIQUE,
	"n_cargo" varchar(15) NOT NULL,
	PRIMARY KEY ("k_CC")
);

CREATE TABLE IF NOT EXISTS "Usuario" (
	"k_CC" serial NOT NULL UNIQUE,
	"n_primerNombre" varchar(50) NOT NULL,
	"n_segundoNombre" varchar(50),
	"n_primerApellido" varchar(50) NOT NULL,
	"n_segundoApellido" varchar(50),
	"f_fechaNacimiento" date NOT NULL,
	"n_correoElectronico" varchar(100) NOT NULL UNIQUE,
	"t_tipoSuscripcion" varchar(50) NOT NULL,
	"f_fechaRegistro" date NOT NULL,
	PRIMARY KEY ("k_CC")
);

CREATE TABLE IF NOT EXISTS "multas" (
	"k_idMulta" serial NOT NULL UNIQUE,
	"v_monto" bigint NOT NULL,
	"n_motivo" varchar(250) NOT NULL,
	"t_estado" varchar(15) NOT NULL,
	"f_fechaEmision" date NOT NULL,
	"k_CCUsuario" bigint NOT NULL,
	PRIMARY KEY ("k_idMulta")
);

CREATE TABLE IF NOT EXISTS "MetodoPago" (
	"k_idMetodoPago" varchar(10) NOT NULL UNIQUE,
	"t_tipoTarjeta" varchar(10) NOT NULL,
	"v_numero" varchar(16) NOT NULL UNIQUE,
	"f_fechaExpiracion" date NOT NULL,
	"n_nombreTitular" varchar(100) NOT NULL UNIQUE,
	"v_saldo" bigint NOT NULL,
	"k_CCUsuario" varchar(11) NOT NULL UNIQUE,
	PRIMARY KEY ("k_idMetodoPago")
);

CREATE TABLE IF NOT EXISTS "Bicicleta" (
	"k_idBicicleta" serial NOT NULL UNIQUE,
	"k_serie" varchar(10) NOT NULL UNIQUE,
	"n_modelo" varchar(50) NOT NULL,
	"t_estado" varchar(25) NOT NULL,
	PRIMARY KEY ("k_idBicicleta")
);

CREATE TABLE IF NOT EXISTS "Viaje" (
	"k_idViaje" serial NOT NULL UNIQUE,
	"f_fechaSolicitud" timestamp with time zone NOT NULL,
	"k_CCUsuario" varchar(11) NOT NULL,
	"k_idBicicleta" varchar(10) NOT NULL,
	PRIMARY KEY ("k_idViaje")
);

CREATE TABLE IF NOT EXISTS "Estacion" (
	"k_idEstacion" serial NOT NULL UNIQUE,
	"n_nombre" varchar(50) NOT NULL UNIQUE,
	"d_calle" varchar(10) NOT NULL,
	"d_carrera" varchar(10) NOT NULL,
	"d_numero" varchar(10) NOT NULL,
	"k_idCiudad" varchar(10) NOT NULL,
	PRIMARY KEY ("k_idEstacion")
);

CREATE TABLE IF NOT EXISTS "ComienzaEn" (
	"k_idViaje" varchar(10) NOT NULL UNIQUE,
	"k_idEstacion" varchar(10) NOT NULL UNIQUE,
	"f_fecha" timestamp with time zone NOT NULL UNIQUE,
	PRIMARY KEY ("k_idViaje", "k_idEstacion", "f_fecha")
);

CREATE TABLE IF NOT EXISTS "TeriminaEn" (
	"k_idViaje" varchar(10) NOT NULL UNIQUE,
	"k_idEstacion" varchar(10) NOT NULL UNIQUE,
	"f_fecha" timestamp with time zone NOT NULL UNIQUE,
	PRIMARY KEY ("k_idViaje", "k_idEstacion", "f_fecha")
);

CREATE TABLE IF NOT EXISTS "Ciudad" (
	"k_idCiudad" varchar(10) NOT NULL UNIQUE,
	"n_nombre" varchar(50) NOT NULL UNIQUE,
	PRIMARY KEY ("k_idCiudad")
);



ALTER TABLE "multas" ADD CONSTRAINT "multas_fk5" FOREIGN KEY ("k_CCUsuario") REFERENCES "Usuario"("k_CC");
ALTER TABLE "MetodoPago" ADD CONSTRAINT "MetodoPago_fk6" FOREIGN KEY ("k_CCUsuario") REFERENCES "Usuario"("k_CC");

ALTER TABLE "Viaje" ADD CONSTRAINT "Viaje_fk2" FOREIGN KEY ("k_CCUsuario") REFERENCES "Usuario"("k_CC");

ALTER TABLE "Viaje" ADD CONSTRAINT "Viaje_fk3" FOREIGN KEY ("k_idBicicleta") REFERENCES "Bicicleta"("k_idBicicleta");
ALTER TABLE "Estacion" ADD CONSTRAINT "Estacion_fk5" FOREIGN KEY ("k_idCiudad") REFERENCES "Ciudad"("k_idCiudad");
ALTER TABLE "ComienzaEn" ADD CONSTRAINT "ComienzaEn_fk0" FOREIGN KEY ("k_idViaje") REFERENCES "Viaje"("k_idViaje");

ALTER TABLE "ComienzaEn" ADD CONSTRAINT "ComienzaEn_fk1" FOREIGN KEY ("k_idEstacion") REFERENCES "Estacion"("k_idEstacion");
ALTER TABLE "TeriminaEn" ADD CONSTRAINT "TeriminaEn_fk0" FOREIGN KEY ("k_idViaje") REFERENCES "Viaje"("k_idViaje");

ALTER TABLE "TeriminaEn" ADD CONSTRAINT "TeriminaEn_fk1" FOREIGN KEY ("k_idEstacion") REFERENCES "Estacion"("k_idEstacion");
