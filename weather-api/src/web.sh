#!/bin/bash

# Leer el JSON completo
JSON_FILE=$1

# Extract the times (dt_txt), temperatures (temp), and cloudiness (clouds.all)
times=$(echo "$JSON_FILE" | jq -r '.list[].dt_txt' | awk 'NR % 4 == 1' | sed 's/$/"/' | sed 's/^/"/' | paste -sd, - | sed 's/","/"," "," ","/g')

temps=$( echo "$JSON_FILE" | jq -r '.list[].main.temp - 273.15' | paste -sd, -)  # Join temperature values with commas
cloudiness=$(echo "$JSON_FILE" | jq -r '.list[].clouds.all' | paste -sd, -)  # Join cloudiness values with commas

# Create an HTML file
HTML_FILE="weather_forecast.html"

# Begin the HTML document
cat <<EOF > $HTML_FILE
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Weather Forecast</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <h1>Weather Forecast</h1>
    <canvas id="weatherChart" width="400px" height="200px"></canvas>  <!-- Smaller canvas size -->

    <script>
        var ctx = document.getElementById('weatherChart').getContext('2d');
        var weatherChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [$times],  // Times
                datasets: [{
                    label: 'Temperature (°C)',
                    data: [$temps],  // Temperatures
                    borderColor: 'rgba(255, 30, 30, 0.6)',
                    borderWidth: 3,  // Increased line thickness
                    fill: false
                },
                {
                    label: 'Cloudiness (%)',
                    data: [$cloudiness],  // Cloudiness
                    borderColor: 'rgba(122, 122, 255, 0.5)',
                    borderWidth: 2,  // Increased line thickness
                    fill: true,
                    yAxisID: 'y1'  // Use a different Y axis for cloudiness
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        min : -10,
                        max : 40
                    },
                    y1: {
                        type: 'linear',
                        position: 'right',
                        beginAtZero: true,
                        max: 120  // Cloudiness is usually between 0 and 100
                    }
                }
            }
        });
    </script>
</body>
</html>
EOF

# Inform the user
echo "HTML file generated: $HTML_FILE"

# Open the file in Firefox
firefox $HTML_FILE
