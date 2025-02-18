#FROM eclipse-temurin:17-jdk
#FROM eclipse-temurin:17-jdk-alpine


# Обновляем переменные окружения
#ENV KAFKA_HOME=/opt/kafka
#ENV PATH=$PATH:$KAFKA_HOME/bin

#WORKDIR /app

#COPY build/libs/kafka-mongodb-1.0-SNAPSHOT.jar app.jar

#CMD ["java", "-cp", "app.jar", "MainKafkaMongo"]
FROM gradle:7.6.2-jdk17 AS build

WORKDIR /app

COPY . /app

RUN gradle build --no-daemon

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /app/build/libs/kafka-mongodb-1.0-SNAPSHOT.jar kafka-app.jar

ENTRYPOINT ["java", "-jar", "kafka-app.jar"]