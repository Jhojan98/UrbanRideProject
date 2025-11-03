FROM openjdk:26-ea-21-jdk-oraclelinux8 AS builder

WORKDIR /app/usuario-service

COPY ./pom.xml /app
COPY ./usuario-service/.mvn ./.mvn
COPY ./usuario-service/mvnw .
COPY ./usuario-service/pom.xml .

RUN ./mvnw clean package -Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip && rm -r ./target/

COPY ./usuario-service/src ./src

RUN ./mvnw clean package -DskipTests

FROM openjdk:26-ea-21-jdk-oraclelinux8

WORKDIR /app
RUN mkdir ./logs
COPY --from=builder /app/usuario-service/target/usuario-service-0.0.1-SNAPSHOT.jar .
EXPOSE 8001

ENTRYPOINT ["java", "-jar", "usuario-service-0.0.1-SNAPSHOT.jar"]

