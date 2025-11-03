FROM openjdk:26-ea-21-jdk-oraclelinux8 AS builder

WORKDIR /app/viaje-service

COPY ./pom.xml /app
COPY ./viaje-service/.mvn ./.mvn
COPY ./viaje-service/mvnw .
COPY ./viaje-service/pom.xml .

RUN ./mvnw clean package -Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip && rm -r ./target/

COPY ./viaje-service/src ./src

RUN ./mvnw clean package -DskipTests

FROM openjdk:26-ea-21-jdk-oraclelinux8

WORKDIR /app
RUN mkdir ./logs
COPY --from=builder /app/viaje-service/target/viaje-service-0.0.1-SNAPSHOT.jar .
EXPOSE 8003

ENTRYPOINT ["java", "-jar", "viaje-service-0.0.1-SNAPSHOT.jar"]