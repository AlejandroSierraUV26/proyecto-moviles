# Etapa 1: Compilar el proyecto y generar el fat jar
FROM gradle:7-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

# Etapa 2: Ejecutar la app
FROM openjdk:11
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/link-saver_server.jar /app/link-saver_server.jar
ENTRYPOINT ["java", "-jar", "/app/link-saver_server.jar"]
