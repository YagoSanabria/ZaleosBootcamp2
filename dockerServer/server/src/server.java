
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.*;

public class server {

    public static void main(String[] args) throws IOException {

        //Create http server that listens on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);

        //Create context for /api/ endpoint
        server.createContext("/api/", new HttpHandler() {

            @Override
            public void handle(HttpExchange exchange) throws IOException {

                switch (exchange.getRequestMethod()) {
                    case "GET":
                        handleGetRequestApi(exchange);
                        break;
                    case "POST":
                        handlePostRequestApi(exchange);
                        break;
                    case "DELETE":
                        handleDeleteRequestApi(exchange);
                        break;
                    default:
                        exchange.sendResponseHeaders(405, -1); //Not permitted method
                        break;
                }
            }
        });

        //create context for /forecast/ endpoint
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
                        handleDeleteRequestForecast(exchange);
                        break;
                    default:
                        exchange.sendResponseHeaders(405, -1); //Not permitted method
                        break;
                }
            }
        });

        //create context for /print/ endpoint
        server.createContext("/graph/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {

                switch (exchange.getRequestMethod()) {
                    case "GET":
                        handleGetRequestPrint(exchange);
                        break;
                    default:
                        exchange.sendResponseHeaders(405, -1); //Not permitted method
                        break;
                }
            }
        });

        //Start the server
        server.setExecutor(null);
        server.start();

        System.out.println("Server started on port 8080");
    }

    //Handle GET request
    private static void handleGetRequestApi(HttpExchange exchange) throws IOException {
        String city = exchange.getRequestURI().toString().substring(5); //5 is /api/ length
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

    //Handle POST request
    private static void handlePostRequestApi(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String jsonText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        try {
            JSONObject json = new JSONObject(jsonText);
            String cityName = json.optString("name", "unknown").replaceAll("[^a-zA-Z0-9_-]", "_");
            cityName = cityName.replaceAll(" ", "").toLowerCase();

            if (cityName.equals("unknown")) {
                String response = "{ \"error\": \"Json doesnt have a valid city name\" }";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            // Save json in file
            String filePath = "db/" + cityName + ".json";
            Files.write(Paths.get(filePath), jsonText.getBytes(StandardCharsets.UTF_8));

            String response = "Data saved successfully in " + filePath;
            System.out.println(response);

            //Response with a simple text message
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (Exception e) {
            String response = "{ \"error\": \"Error processing JSON\" }";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    //Handle DELETE request
    private static void handleDeleteRequestApi(HttpExchange exchange) throws IOException {
        System.out.println("api DELETE request");
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("filePath=")) {
            String response = "{ \"error\": \"Not valid filepath\" }";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }

        String filePath = URLDecoder.decode(query.substring(9), StandardCharsets.UTF_8);
        Path path = Paths.get(filePath);

        try {
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("File deleted successfully: " + filePath);
                String response = "File deleted successfully: " + filePath;
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                String response = "{ \"error\": \"File does not exist\" }";
                exchange.sendResponseHeaders(404, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        } catch (IOException e) {
            String response = "{ \"error\": \"Error while deleting file\" }";
            exchange.sendResponseHeaders(500, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    //Handle GET request
    private static void handleGetRequestForecast(HttpExchange exchange) throws IOException {
        String city = exchange.getRequestURI().toString().substring(10); //10 is /forecast/ length
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

    //Handle POST request
    private static void handlePostRequestForecast(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String jsonText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        try {
            JSONObject json = new JSONObject(jsonText);
            JSONObject city = json.getJSONObject("city");
            String cityName = city.optString("name", "unknown").replaceAll("[^a-zA-Z0-9_ -]", "_");
            cityName = cityName.replaceAll(" ", "").toLowerCase();

            if (cityName.equals("unknown")) {
                System.out.println("error file name");
                String response = "{ \"error\": \"Json does not have valid city name\" }";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            // Save json in file
            String filePath = "db/forecast/" + cityName + ".json";

            System.out.println("filePath: " + filePath);

            Files.write(Paths.get(filePath), jsonText.getBytes(StandardCharsets.UTF_8));

            String response = "Data successfully saved in " + filePath;
            System.out.println(response);

            //Anwser with a simple text message
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }

        } catch (Exception e) {
            String response = "{ \"error\": \"error processing json\" }";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    //Handle DELETE request
    private static void handleDeleteRequestForecast(HttpExchange exchange) throws IOException {
        System.out.println("forecast DELETE request");
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("filePath=")) {
            String response = "{ \"error\": \"Not valid filepath\" }";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }

        String filePath = URLDecoder.decode(query.substring(9), StandardCharsets.UTF_8);
        Path path = Paths.get(filePath);

        try {
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("File deleted successfully: " + filePath);
                String response = "File deleted successully " + filePath;
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                String response = "{ \"error\": \"File does not exist\" }";
                exchange.sendResponseHeaders(404, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        } catch (IOException e) {
            String response = "{ \"error\": \"Error while deleting file\" }";
            exchange.sendResponseHeaders(500, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    //Handle GET request
    private static void handleGetRequestPrint(HttpExchange exchange) throws IOException {
        String city = exchange.getRequestURI().toString().substring(7); //7 is /print/ length
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
}
