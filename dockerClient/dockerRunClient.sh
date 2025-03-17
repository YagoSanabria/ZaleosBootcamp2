#!/bin/bash

# Ejecutar el contenedor
sudo docker run -v $(pwd)/workspace:/app/workspace -it --rm weather-api
