# ZaleosBootcamp2 - Docker & API

Repository with REST-API that reaches to our personal API of weather forecast in Docker-compose (GNU Linux-based).
By Luna Asegurado & Yago Sanabria

## How to use
After downloading this repository you may need some insight in how to use our application in your system. We have developed it in a GNU-Linux environment and using java language (java-21 or higher)

### Server side:
To be able to run propperly our containers you need some mandatory files (already included):

#### docker-compose.yaml
```yaml
services:
    weather-api:
        build: .
        container_name: server
        ports:
            - "8080:8080"
        volumes:
            - db:/app/db 
        networks:
            - custom_network  #Custom network name
networks:
    custom_network:  # Defining the custom network
        driver: bridge  #Bridge mode for isolated communication
```
#### Dockerfile
```Dockerfile
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
```

In `dockerServer/` we have included some bash scripts to help the user **build** and **run** the containers the right way.    

```bash 
sudo docker build -t server .
```

```bash 
sudo docker run -it -p 8080:8080 --rm server
```

---

### Client side:
To interact with the server, you need to set up the client container as well. Here are the necessary files:

#### docker-compose.yaml
```yaml
services:
  weather-api:
    build: .
    container_name: weather_api
    ports:
      - "8080:8080"

```

#### Dockerfile (client)
```Dockerfile
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
```

In `dockerClient/` we have included some bash scripts to help the user **build** and **run** the client container the right way.

```bash 
sudo docker build -t weather-api .
```

```bash 
sudo docker run -v $(pwd)/workspace:/app/workspace -it --rm weather-api
```