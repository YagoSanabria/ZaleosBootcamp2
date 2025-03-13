
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class SimpleApi {

    public static void main(String[] args) throws IOException {

        //Create http server that listens on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);

        //Define handler for GET "/api"
        server.createContext("/api/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {

                String city = exchange.getRequestURI().toString().substring(4); //4 is /api length
                System.out.println("\nCity name: " + city);

                //check if city is in db
                File file = new File("../db/" + city + ".json");

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
        });

        //Define handler for GET "/forecast"
        server.createContext("/forecast/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {

                String city = exchange.getRequestURI().toString().substring(9); //9 is /forecast length
                System.out.println("\nCity name: " + city);

                //check if city is in db
                File file = new File("../db/forecast/" + city + ".json");

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
        });

        //Start the server
        server.setExecutor(null);
        server.start();

        System.out.println("Servidor iniciado en http://localhost:8080");
    }
}
