import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherApi {

    public static void main(String[] args) {
        String apiKey = "d59cfe8cb6e6d6f23b2eee3625f1e8d2";  // Sustituye con tu propia clave de API
        String city = "Madrid";  // Cambia a la ciudad que quieras
        String apiUrl = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey;

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

                // Mostrar el resultado (la respuesta de la API)
                System.out.println("Respuesta de OpenWeather: ");
                System.out.println(response.toString());
            } else {
                System.out.println("Error en la solicitud. Código de respuesta: " + status);
            }

            // Cerrar la conexión
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
