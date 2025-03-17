FROM openjdk:21-jdk-slim
WORKDIR /app

#Copy the files and libraries to the container
COPY weather-api/lib/json-lib.jar .
COPY weather-api/src/WeatherApi.java .
COPY . /app/workspace


#Compiple the java file with the library
RUN javac -cp json-lib.jar WeatherApi.java

#Run the java file with the library
CMD ["java", "-cp", ".:json-lib.jar", "WeatherApi"]
