
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONObject;

public class WeatherApi {

    public static void main(String[] args) {
        String apiKey = "d59cfe8cb6e6d6f23b2eee3625f1e8d2";
        String city = "Madrid";  //Default city
        Scanner scanner = new Scanner(System.in);
        while (true) {

            System.out.print("Insert city name (exit to quit): ");
            city = scanner.nextLine();

            if (city.equals("exit")) {
                break;
            } else if (city.equals("")) {
                continue;
            }

            city = city.replace(" ", "%20");

            String apiUrl = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&units=metric";
            //System.out.println("API URL: " + apiUrl);
            try {
                URL url = new URL(apiUrl);

                //open http connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);  // Timeout de conexión de 5 segundos
                connection.setReadTimeout(5000);  // Timeout de lectura de 5 segundos

                //http response code
                int status = connection.getResponseCode();

                //all ok (code 200)
                if (status == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    //System.out.println("API Response: " + response.toString());
                    JSONObject jsonResponse = new JSONObject(response.toString());

                    //Get data from json
                    String cityName = jsonResponse.getString("name");
                    String country = jsonResponse.getJSONObject("sys").getString("country");
                    double temperature = jsonResponse.getJSONObject("main").getDouble("temp");

                    double tempLike = jsonResponse.getJSONObject("main").getDouble("feels_like");

                    double humidity = jsonResponse.getJSONObject("main").getDouble("humidity");
                    String weatherDescription = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("main") + ", " + jsonResponse.getJSONArray("weather").getJSONObject(0).getString("description");

                    double coordX = jsonResponse.getJSONObject("coord").getDouble("lat");
                    double coordY = jsonResponse.getJSONObject("coord").getDouble("lon");

                    String coordXtxt = (coordX < 0) ? Math.abs(coordX) + "S" : coordX + "N";
                    String coordYtxt = (coordY < 0) ? Math.abs(coordY) + "W" : coordY + "E";

                    //Print data in terminal
                    System.out.println("\nWeather in " + cityName + "(" + country + "), " + "(" + coordXtxt + "," + coordYtxt + "):");
                    System.out.println("\tDescription: " + weatherDescription);
                    System.out.println("\tTemperature: " + temperature + "°C, feels like " + tempLike + "°C");
                    System.out.println("\tHumidity: " + humidity + "%\n");

                } else {
                    System.out.println("Error in request. Response code: " + status);
                }

                //close connection
                connection.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        scanner.close();
        System.out.println("Exiting program...");
    }
}
