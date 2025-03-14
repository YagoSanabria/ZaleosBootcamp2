
#!/bin/bash

# Definir la ruta al JAR de JSON
JSON_LIB=".:server/lib/json-lib.jar"

# Compilar el código con el JAR en el classpath
javac -cp "$JSON_LIB" server/src/server.java

# Cambiar al directorio donde está el código fuente
cd server/src

# Ejecutar el servidor con el JAR en el classpath
java --add-modules jdk.httpserver -cp ".:../lib/json-lib.jar" server

# Regresar al directorio original
cd ../..
