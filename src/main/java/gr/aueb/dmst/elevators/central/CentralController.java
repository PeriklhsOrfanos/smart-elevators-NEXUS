
package gr.aueb.dmst.elevators.central;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CentralController {

    private final int numberOfElevators;
    private final int numberOfFloors;
    private final DispatchAlgorithm dispatchAlgorithm;

    private HttpServer server;

    public CentralController(int elevators, int floors) {
        if (elevators < 2 || elevators > 20) {
            throw new IllegalArgumentException(
                "Elevators must be between 2 and 20"
            );
        }

        if (floors < 3 || floors > 200) {
            throw new IllegalArgumentException(
                "Floors must be between 3 and 200"
            );
        }

        this.numberOfElevators = elevators;
        this.numberOfFloors = floors;
        this.dispatchAlgorithm = new DispatchAlgorithm(elevators, floors);
    }

    public static void main(String[] args) {
        try {
            int elevators = args.length > 0
                ? Integer.parseInt(args[0]) : 4;

            int floors = args.length > 1
                ? Integer.parseInt(args[1]) : 10;

            CentralController controller =
                new CentralController(elevators, floors);

            controller.start();

        } catch (IllegalArgumentException e) {
            System.err.println(
                "Invalid configuration: " + e.getMessage()
            );
        }
    }

    public void start() {
        try {
            server = HttpServer.create(
                new InetSocketAddress(8080), 0
            );

            server.createContext(
                "/api/health", this::handleHealth
            );

            server.createContext(
                "/api/requests", this::handleRequest
            );

            server.start();

            System.out.println("NEXUS Central Controller started");
            System.out.println("Elevators: " + numberOfElevators);
            System.out.println("Floors: " + numberOfFloors);
            System.out.println("REST API: http://localhost:8080");

        } catch (IOException e) {
            System.err.println(
                "Failed to start server: " + e.getMessage()
            );
        }
    }

    private void handleHealth(HttpExchange exchange)
            throws IOException {

        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405,
                "{\"error\":\"Method not allowed\"}");
            return;
        }

        sendResponse(exchange, 200, "{\"status\":\"UP\"}");
    }

    private void handleRequest(HttpExchange exchange)
            throws IOException {

        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405,
                "{\"error\":\"Method not allowed\"}");
            return;
        }

        String body = new String(
            exchange.getRequestBody().readAllBytes(),
            StandardCharsets.UTF_8
        );

        try {
            int originFloor = readInteger(body, "originFloor");
            int destinationFloor =
                readInteger(body, "destinationFloor");

            if (originFloor < 1 || originFloor > numberOfFloors
                    || destinationFloor < 1
                    || destinationFloor > numberOfFloors) {

                sendResponse(exchange, 400,
                    "{\"error\":\"Invalid floor\"}");
                return;
            }

            if (originFloor == destinationFloor) {
                sendResponse(exchange, 400,
                    "{\"error\":\"Origin and destination must differ\"}");
                return;
            }

            String assignedElevator =
                dispatchAlgorithm.assignElevator(originFloor);

            String response =
                "{\"assignedElevator\":\"" + assignedElevator
                + "\",\"originFloor\":" + originFloor
                + ",\"destinationFloor\":" + destinationFloor
                + "}";

            System.out.println(
                "Request: " + originFloor + " -> "
                + destinationFloor
                + " | Assigned: " + assignedElevator
            );

            sendResponse(exchange, 200, response);

        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400,
                "{\"error\":\"Invalid request data\"}");
        }
    }

    private static int readInteger(String json, String field) {
        Pattern pattern = Pattern.compile(
            "\"" + field + "\"\\s*:\\s*(-?\\d+)"
        );

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing " + field);
        }

        return Integer.parseInt(matcher.group(1));
    }

    private void sendResponse(
            HttpExchange exchange,
            int status,
            String response) throws IOException {

        byte[] bytes =
            response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
