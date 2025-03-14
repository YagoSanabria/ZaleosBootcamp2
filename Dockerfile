FROM openjdk:17-jdk-slim
WORKDIR /app

# Copiar el código fuente y la librería JSON
COPY weather-api/src/WeatherApi.java .
COPY weather-api/lib/json-lib.jar .

# Compilar el código incluyendo la librería externa
RUN javac -cp json-lib.jar WeatherApi.java

# Ejecutar el programa con la librería en el classpath
CMD ["java", "-cp", ".:json-lib.jar", "WeatherApi"]

