
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject; // Necesitarás agregar esta librería si usas esta opción
import java.util.Scanner;

public class WeatherApi {

    public static void main(String[] args) {
        String apiKey = "d59cfe8cb6e6d6f23b2eee3625f1e8d2";  // Sustituye con tu propia clave de API
        String city = "Madrid";  // Cambia a la ciudad que quieras
        Scanner scanner = new Scanner(System.in);
        while (true) {

            System.out.print("Insert city name (exit to quit): ");
            city = scanner.nextLine();

            if (city.equals("exit")) {
                break;
            }

            city = city.replace(" ", "%20");

            String apiUrl = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&units=metric";
            //System.out.println("API URL: " + apiUrl);
            try {
                // Crear una URL con la URL de la API
                URL url = new URL(apiUrl);

                // Abrir una conexión HTTP a la URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);  // Timeout de conexión de 5 segundos
                connection.setReadTimeout(5000);  // Timeout de lectura de 5 segundos

                // Obtener el código de respuesta HTTP
                int status = connection.getResponseCode();

                // Si la respuesta es exitosa (código 200), leer la respuesta
                if (status == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Convertir la respuesta a un JSONObject
                    JSONObject jsonResponse = new JSONObject(response.toString());

                    // Extraer los datos del JSON
                    String cityName = jsonResponse.getString("name");
                    double temperature = jsonResponse.getJSONObject("main").getDouble("temp");
                    double humidity = jsonResponse.getJSONObject("main").getDouble("humidity");
                    String weatherDescription = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("description");

                    // Mostrar el resultado de forma más legible
                    System.out.println("Weather en " + cityName + ":");
                    System.out.println("Description: " + weatherDescription);
                    System.out.println("Temperature: " + temperature + "°C");
                    System.out.println("Humidity: " + humidity + "%\n");

                } else {
                    System.out.println("Error in request. Response code: " + status);
                }

                // Cerrar la conexión
                connection.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        scanner.close();
        System.out.println("Exiting program...");
    }
}
