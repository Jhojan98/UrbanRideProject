FROM eclipse-temurin:21-jdk AS builder
ARG SERVICE_NAME=notification-email-service
WORKDIR /app

# Copiar POM raíz y el módulo para resolver el parent correctamente
COPY ./pom.xml ./pom.xml
COPY ./${SERVICE_NAME}/.mvn ./.mvn
COPY ./${SERVICE_NAME}/mvnw ./mvnw
COPY ./${SERVICE_NAME}/pom.xml ./${SERVICE_NAME}/pom.xml
COPY ./${SERVICE_NAME}/src ./${SERVICE_NAME}/src

# Compilar el módulo usando el POM del módulo (con parent en ../pom.xml)
RUN chmod +x ./mvnw && sed -i 's/\r$//' mvnw \
    && ./mvnw -f ${SERVICE_NAME}/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre
ARG SERVICE_NAME=notification-email-service
WORKDIR /app
RUN mkdir -p ./logs
# Copiar el artefacto construido desde el builder
COPY --from=builder /app/${SERVICE_NAME}/target/${SERVICE_NAME}-0.0.1-SNAPSHOT.jar ./notification-email-service.jar

ENTRYPOINT ["java", "-jar", "/app/notification-email-service.jar"]
