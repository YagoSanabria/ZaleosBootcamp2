import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;


public class SimpleApi {

    public static void main(String[] args) throws IOException {
        // Crear un servidor HTTP que escucha en el puerto 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Definir el manejador para el endpoint GET "/api"
        server.createContext("/api", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {

                

                String uri = exchange.getRequestURI().toString();

                String city = uri.substring(4); //4 is /api length

                System.out.println("City name: " + city);

                //check if city is in db

                File file = new File("../db/" + city + ".json");

                if (!file.exists()) {
                    String response = "{message: \"City not found\"}";
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
        
        // Iniciar el servidor
        server.setExecutor(null); // Usa el ejecutor predeterminado
        server.start();

        System.out.println("Servidor iniciado en http://localhost:8080");
    }
}
