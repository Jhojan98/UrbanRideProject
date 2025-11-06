FROM openjdk:26-ea-21-jdk-oraclelinux8 AS builder
ARG SERVICE_NAME=viaje-service
WORKDIR /app/$SERVICE_NAME

COPY ./pom.xml /app
COPY ./$SERVICE_NAME/.mvn ./.mvn
COPY ./$SERVICE_NAME/mvnw .
COPY ./$SERVICE_NAME/pom.xml .

RUN ./mvnw clean package -Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip && rm -r ./target/

COPY ./$SERVICE_NAME/src ./src

RUN ./mvnw clean package -DskipTests

FROM openjdk:26-ea-21-jdk-oraclelinux8

WORKDIR /app
RUN mkdir ./logs
ARG SERVICE_NAME=viaje-service
ARG TARGET_FOLDER=/app/$SERVICE_NAME/target/
COPY --from=builder $TARGET_FOLDER/viaje-service-0.0.1-SNAPSHOT.jar .
ARG PORT_APP=8003
ENV PORT=$PORT_APP
EXPOSE $PORT

ENTRYPOINT ["java", "-jar", "viaje-service-0.0.1-SNAPSHOT.jar"]