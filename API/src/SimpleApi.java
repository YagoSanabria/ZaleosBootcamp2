import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
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

                System.out.println(uri);

                uri = uri.substring(4); //4 is /apia length

                System.out.println(uri);

                String response = "{message: \"¡Hola: " + uri + " desde tu API en Java!\"}";
                exchange.sendResponseHeaders(200, response.getBytes().length);

                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());

                System.out.println("Send response");

                os.close();
            }
        });
        
        // Iniciar el servidor
        server.setExecutor(null); // Usa el ejecutor predeterminado
        server.start();

        System.out.println("Servidor iniciado en http://localhost:8080");
    }
}
