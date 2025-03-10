FROM openjdk:17-jdk-slim

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo .jar de tu aplicación Java al contenedor
COPY target/weather-api.jar /app/weather-api.jar

# Expón el puerto donde estará corriendo la API (por ejemplo, el 8080)
EXPOSE 8080

# Comando para ejecutar la aplicación Java
CMD ["java", "-jar", "/app/weather-api.jar"]

