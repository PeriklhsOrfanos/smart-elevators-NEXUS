package gr.aueb.dmst.elevators;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class CentralController {
    private int numberOfElevators;
    private int numberOfFloors;
    private HttpServer server;

    public CentralController(int numberOfElevators, int numberOfFloors) {
        this.numberOfElevators = numberOfElevators;
        this.numberOfFloors = numberOfFloors;
    }

    public static void main(String[] args) {
        // Διαβάζουμε παραμέτρους από τη γραμμή εντολών (CLI)
        int elevators = args.length > 0 ? Integer.parseInt(args[0]) : 4;
        int floors = args.length > 1 ? Integer.parseInt(args[1]) : 10;

        System.out.println("Central Control Module starting...");
        CentralController controller = new CentralController(elevators, floors);
        controller.start();
    }

    public void start() {
        System.out.println("Central Control active for " 
            + numberOfElevators + " elevators and " 
            + numberOfFloors + " floors.");

        try {
            // Δημιουργία REST HTTP Server στη θύρα 8080
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            
            // Endpoint για λήψη αιτημάτων από τη Διεπαφή Χρήστη
            server.createContext("/api/requests", new RequestHandler());
            
            server.setExecutor(null); // Default executor
            server.start();
            System.out.println("REST API server running on port 8080");
        } catch (IOException e) {
            System.err.println("Failed to start HTTP server: " + e.getMessage());
        }
    }

    // Handler για τα REST αιτήματα
    static class RequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                // Εδώ θα μπει η λογική επιλογής ανελκυστήρα
                String response = "{\"assignedElevator\": \"A\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(45, -1); // Method Not Allowed
            }
        }
    }
}