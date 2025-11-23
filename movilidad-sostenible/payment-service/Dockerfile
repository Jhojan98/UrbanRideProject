FROM openjdk:26-ea-21-jdk-oraclelinux8 AS builder
ARG SERVICE_NAME=payment-service
WORKDIR /app/$SERVICE_NAME

COPY ./pom.xml /app
COPY ./$SERVICE_NAME/.mvn ./.mvn
COPY ./$SERVICE_NAME/mvnw .
COPY ./$SERVICE_NAME/pom.xml .

RUN chmod +x mvnw && sed -i 's/\r$//' mvnw

# Compila sin empaquetar jar final (calienta dependencias)
RUN ./mvnw clean package -Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip && rm -r ./target/

COPY ./$SERVICE_NAME/src ./src

RUN ./mvnw clean package -DskipTests

FROM openjdk:26-ea-21-jdk-oraclelinux8
WORKDIR /app
RUN mkdir ./logs
ARG SERVICE_NAME=payment-service
ARG TARGET_FOLDER=/app/$SERVICE_NAME/target/
COPY --from=builder $TARGET_FOLDER/payment-service-0.0.1-SNAPSHOT.jar .
ARG PORT_APP=8007
ENV PORT=$PORT_APP
EXPOSE $PORT
# Variables de entorno para Stripe (puedes sobreescribir en docker-compose)
ENV STRIPE_SECRET_KEY=sk_test_colocaTuClave \
    STRIPE_PUBLISHABLE_KEY=pk_test_colocaTuClave \
    STRIPE_WEBHOOK_SECRET=whsec_colocaTuWebhookSecret

ENTRYPOINT ["java", "-jar", "payment-service-0.0.1-SNAPSHOT.jar"]

