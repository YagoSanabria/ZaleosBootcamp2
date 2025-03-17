
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.*;

public class server {

    public static void main(String[] args) throws IOException {

        //Create http server that listens on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);

        //Define handler for GET "/api"
        server.createContext("/api/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {

                switch (exchange.getRequestMethod()) {
                    case "GET":
                        handleGetRequestAPI(exchange);
                        break;
                    case "POST":
                        handlePostRequestAPI(exchange);
                        break;
                    case "DELETE":
                        //handleDeleteRequestAPI(exchange);
                        break;
                    default:
                        exchange.sendResponseHeaders(405, -1); // Método no permitido
                        break;
                }
            }
        });

        //Define handler for GET "/forecast"
        server.createContext("/forecast/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {

                switch (exchange.getRequestMethod()) {
                    case "GET":
                        handleGetRequestForecast(exchange);
                        break;
                    case "POST":
                        handlePostRequestForecast(exchange);
                        break;
                    case "DELETE":
                        //handleDeleteRequestForecast(exchange);
                        break;
                    default:
                        exchange.sendResponseHeaders(405, -1); // Método no permitido
                        break;
                }
            }
        }
        );

        //Start the server
        server.setExecutor(null);
        server.start();

        System.out.println("Servidor iniciado en http://192.168.0.231:8080/");
    }

    private static void handleGetRequestAPI(HttpExchange exchange) throws IOException {
        String city = exchange.getRequestURI().toString().substring(5); //4 is /api length
        System.out.println("\nCity name: " + city);

        //check if city is in db
        File file = new File("db/" + city + ".json");

        if (!file.exists()) {
            String response = "{Error message: \"" + city + " not found\"}";
            exchange.sendResponseHeaders(404, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            System.out.println("Send response from " + city);
            byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, fileBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();
        }
    }

    private static void handlePostRequestAPI(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String jsonText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        try {
            JSONObject json = new JSONObject(jsonText);
            String cityName = json.optString("name", "unknown").replaceAll("[^a-zA-Z0-9_-]", "_");
            cityName = cityName.replaceAll(" ", "").toLowerCase();

            if (cityName.equals("unknown")) {
                String response = "{ \"error\": \"El JSON no contiene un nombre de ciudad válido\" }";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            // Guardar el JSON en un archivo
            String filePath = "db/" + cityName + ".json";
            Files.write(Paths.get(filePath), jsonText.getBytes(StandardCharsets.UTF_8));

            String response = "Datos guardados correctamente en " + filePath;
            System.out.println(response); // Imprimir mensaje en el servidor

            // Responder con un simple mensaje de texto
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (Exception e) {
            String response = "{ \"error\": \"Error procesando el JSON\" }";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            } catch (IOException err) {
                e.printStackTrace();
            }
        }
    }

    private static void handleGetRequestForecast(HttpExchange exchange) throws IOException {
        String city = exchange.getRequestURI().toString().substring(10); //9 is /forecast length
        System.out.println("\nCity name: " + city);

        //check if city is in db
        File file = new File("db/forecast/" + city + ".json");

        if (!file.exists()) {
            String response = "{Error message: \"" + city + " not found\"}";
            exchange.sendResponseHeaders(404, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            System.out.println("Send response from " + city);
            byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, fileBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();
        }
    }

    private static void handlePostRequestForecast(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String jsonText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        try {
            JSONObject json = new JSONObject(jsonText);
            JSONObject city = json.getJSONObject("city");
            String cityName = city.optString("name", "unknown").replaceAll("[^a-zA-Z0-9_ -]", "_");
            cityName = cityName.replaceAll(" ", "").toLowerCase();
            System.out.println(cityName);

            if (cityName.equals("unknown")) {
                System.out.println("error nombre archivo");
                String response = "{ \"error\": \"El JSON no contiene un nombre de ciudad válido\" }";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            // Guardar el JSON en un archivo
            String filePath = "db/forecast/" + cityName + ".json";

            System.out.println("filePath: " + filePath);

            Files.write(Paths.get(filePath), jsonText.getBytes(StandardCharsets.UTF_8));

            String response = "Datos guardados correctamente en " + filePath;
            System.out.println(response); // Imprimir mensaje en el servidor

            // Responder con un simple mensaje de texto
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }

        } catch (Exception e) {
            String response = "{ \"error\": \"Error procesando el JSON\" }";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            } catch (IOException err) {
                e.printStackTrace();
            }
        }
    }
}
