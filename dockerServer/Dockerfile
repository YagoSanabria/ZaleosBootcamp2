FROM openjdk:21-jdk-slim
WORKDIR /app

# Copiar el código fuente y la librería JSON
COPY server/src/server.java .
COPY server/lib/json-lib.jar .
COPY db /app/db

# Compilar el código incluyendo la librería externa
RUN javac -cp json-lib.jar server.java

# Ejecutar el programa con la librería en el classpath
CMD ["java", "-cp", ".:json-lib.jar", "server"]

