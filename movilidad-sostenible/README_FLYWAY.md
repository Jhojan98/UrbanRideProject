# Flyway y roles para BD compartida

Este proyecto usa una sola base de datos `movilidad_sostenible` y un único esquema `public`. Las migraciones de esquema se aplican desde `viaje-service` usando Flyway. Cada microservicio se conecta con un usuario dedicado de mínimos privilegios.

## Estructura
- Migraciones: `viaje-service/src/main/resources/db/migration/`
  - `V1__init.sql`: crea todas las tablas base.
- Usuarios/roles: `db/script_roles.sql` (crear roles y otorgar privilegios).

## Pasos de uso
1. Levanta Postgres:
```bash
docker compose up -d postgres17
```
2. (Opcional limpieza) Si quieres una BD limpia:
```bash
docker compose down -v && docker compose up -d postgres17
```
3. Ejecuta script de roles (dentro del contenedor o con cliente local):
```bash
docker exec -i <postgres_container_id> psql -U postgres -d movilidad_sostenible < db/script_roles.sql
```
4. Arranca viaje-service (aplica migraciones) pasando credenciales del usuario `flyway`:
```bash
export POSTGRES_USER_FLYWAY=flyway
export POSTGRES_PASSWORD_FLYWAY=FLYWAY_PASS
# Si la BD ya tiene objetos y quieres adoptar el estado como baseline (no común), activa:
# export FLYWAY_BASELINE=true
cd viaje-service
./mvnw spring-boot:run
```
Revisa en logs: `Successfully applied 1 migration`.

5. Arranca el resto de servicios normalmente (usan sus propios usuarios):
```bash
docker compose up -d --build
```

## Notas
- No se permite `clean` en Flyway en producción (`spring.flyway.clean-disabled=true`).
- Hibernate no genera esquema (`spring.jpa.hibernate.ddl-auto=none`).
- Cambia las contraseñas por variables de entorno en despliegue real.
- Si cambias el esquema en el futuro, agrega `V2__...sql` y repite el paso 4.

