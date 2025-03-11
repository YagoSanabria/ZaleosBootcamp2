#!/bin/bash

# Ejecutar el contenedor
API_KEY=$(cat ../key.txt)  # Leer la clave del archivo
sudo docker run -it --rm -e API_KEY="$API_KEY" weather-api
