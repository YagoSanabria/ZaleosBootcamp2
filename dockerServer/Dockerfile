FROM openjdk:21-jdk-slim
WORKDIR /app

#Copy the files and libraries to the container
COPY server/lib/json-lib.jar .
COPY server/src/server.java .
COPY db /app/db

#Compile the server using the external library
RUN javac -cp json-lib.jar server.java

#Run the server with the external library
CMD ["java", "-cp", ".:json-lib.jar", "server"]
