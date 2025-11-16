FROM openjdk:26-ea-21-jdk-oraclelinux8 AS builder
ARG SERVICE_NAME=database-migrator
WORKDIR /app/$SERVICE_NAME

# Copiar POM raíz para resolver dependencias del reactor
COPY ./pom.xml /app
# Copiar wrapper y POM del servicio
COPY ./$SERVICE_NAME/.mvn ./.mvn
COPY ./$SERVICE_NAME/mvnw .
COPY ./$SERVICE_NAME/pom.xml .

# Asegurar permisos de ejecución del wrapper y normalizar EOL
RUN chmod +x mvnw && sed -i 's/\r$//' mvnw

# Descarga dependencias sin compilar fuentes todavía
RUN ./mvnw clean package -Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip && rm -r ./target/

# Copiar el código fuente y empaquetar
COPY ./$SERVICE_NAME/src ./src
RUN ./mvnw clean package -DskipTests

# Runtime image
FROM openjdk:26-ea-21-jdk-oraclelinux8
WORKDIR /app
RUN mkdir ./logs
ARG SERVICE_NAME=database-migrator
ARG TARGET_FOLDER=/app/$SERVICE_NAME/target/
COPY --from=builder $TARGET_FOLDER/database-migrator-0.0.1-SNAPSHOT.jar .

ENV PORT=0
ENTRYPOINT ["java", "-jar", "database-migrator-0.0.1-SNAPSHOT.jar"]

