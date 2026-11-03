import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Elevator {
    
    static int destinationFloor = 0;

    public static void main(String[] args) throws IOException {
        
        int installationFloor = 0;
        if (args.length > 0) {
            installationFloor = Integer.parseInt(args[0]);
        }
        
        char elevatorLetter = 'A'; 

        System.out.println("Elevator: " + elevatorLetter);
        System.out.println("Installation floor: " + installationFloor);

        Scanner scanner = new Scanner(System.in);
        System.out.print("Give destination floor: ");
        if (scanner.hasNextInt()) {
            destinationFloor = scanner.nextInt();
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/elevator", new ElevatorHandler(installationFloor, elevatorLetter));
        server.setExecutor(null); 
        server.start();
        
        System.out.println("O REST server ksekinhse stin porta 8080...");
    }

    static class ElevatorHandler implements HttpHandler {
        int installFloor;
        char letter;

        public ElevatorHandler(int installFloor, char letter) {
            this.installFloor = installFloor;
            this.letter = letter;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\n" +
                    "  \"elevator\": \"" + letter + "\",\n" +
                    "  \"installation_floor\": " + installFloor + ",\n" +
                    "  \"destination_floor\": " + destinationFloor + "\n" +
                    "}";

            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}