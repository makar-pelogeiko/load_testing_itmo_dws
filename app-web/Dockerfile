FROM gradle:8.12.1-jdk21 AS build

WORKDIR /app

COPY . /app

RUN gradle build -x test --no-daemon

FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=build /app/build/libs/app-web-0.0.1-SNAPSHOT.jar web-app.jar

ENTRYPOINT ["java", "-jar", "web-app.jar"]