FROM openjdk:26-ea-21-jdk-oraclelinux8 AS builder

WORKDIR /app/bicis-service

COPY ./pom.xml /app
COPY ./bicis-service/.mvn ./.mvn
COPY ./bicis-service/mvnw .
COPY ./bicis-service/pom.xml .

RUN ./mvnw clean package -Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip && rm -r ./target/

COPY ./bicis-service/src ./src

RUN ./mvnw clean package -DskipTests

FROM openjdk:26-ea-21-jdk-oraclelinux8

WORKDIR /app
RUN mkdir ./logs
COPY --from=builder /app/bicis-service/target/bicis-service-0.0.1-SNAPSHOT.jar .
EXPOSE 8002

ENTRYPOINT ["java", "-jar", "bicis-service-0.0.1-SNAPSHOT.jar"]