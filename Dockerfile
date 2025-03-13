FROM openjdk:21-jdk-slim
WORKDIR /app

# Copiar el código fuente y las librerías externas
COPY weather-api/src/WeatherApi.java .
COPY weather-api/lib/json-lib.jar .
COPY javafx-sdk-23.0.2/lib/javafx.base.jar .
COPY javafx-sdk-23.0.2/lib/javafx.controls.jar .
COPY javafx-sdk-23.0.2/lib/javafx.graphics.jar .

# Copiar las bibliotecas .so de JavaFX (Native libraries)
COPY javafx-sdk-23.0.2/lib/*.so /usr/lib/javafx/

# Establecer la variable de entorno para encontrar las bibliotecas .so de JavaFX
ENV LD_LIBRARY_PATH=/usr/lib/javafx:$LD_LIBRARY_PATH

# Compilar el código incluyendo la librería externa
RUN javac -cp .:json-lib.jar:javafx.base.jar:javafx.controls.jar:javafx.graphics.jar WeatherApi.java -d weather-api/target

# Ejecutar el programa con las bibliotecas en el classpath y agregar JavaFX
CMD ["java", "-cp", ".:weather-api/target:json-lib.jar:javafx.base.jar:javafx.controls.jar:javafx.graphics.jar", "--module-path", "/app/javafx-sdk-23.0.2/lib", "--add-modules", "javafx.base,javafx.controls,javafx.graphics", "WeatherApi"]

