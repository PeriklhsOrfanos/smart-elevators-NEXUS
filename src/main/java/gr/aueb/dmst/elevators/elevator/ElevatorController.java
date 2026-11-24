
package gr.aueb.dmst.elevators.elevator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class ElevatorController {

    private final char elevatorId;
    private final int numberOfFloors;
    private int currentFloor;
    private String status;

    private HttpServer server;

    public ElevatorController(
            char elevatorId,
            int initialFloor,
            int numberOfFloors) {

        if (elevatorId < 'A' || elevatorId > 'T') {
            throw new IllegalArgumentException(
                "Elevator ID must be between A and T"
            );
        }

        if (numberOfFloors < 3 || numberOfFloors > 200) {
            throw new IllegalArgumentException(
                "Invalid number of floors"
            );
        }

        if (initialFloor < 1 || initialFloor > numberOfFloors) {
            throw new IllegalArgumentException(
                "Invalid initial floor"
            );
        }

        this.elevatorId = elevatorId;
        this.currentFloor = initialFloor;
        this.numberOfFloors = numberOfFloors;
        this.status = "IDLE";
    }

    public static void main(String[] args) {

        if (args.length != 4) {
            System.out.println(
                "Usage: ElevatorController <ID> <floor> <floors> <port>"
            );
            return;
        }

        try {
            if (args[0].length() != 1) {
                throw new IllegalArgumentException("Invalid elevator ID");
            }

            char id = args[0].charAt(0);
            int floor = Integer.parseInt(args[1]);
            int floors = Integer.parseInt(args[2]);
            int port = Integer.parseInt(args[3]);

            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Invalid port");
            }

            ElevatorController elevator =
                new ElevatorController(id, floor, floors);

            elevator.start(port);

        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void start(int port) {

        try {
            server = HttpServer.create(
                new InetSocketAddress(port), 0
            );

            server.createContext(
                "/api/elevator/status", this::handleStatus
            );

            server.createContext(
                "/api/elevator/move", this::handleMove
            );

            server.start();

            System.out.println("NEXUS Elevator " + elevatorId);
            System.out.println("Initial floor: " + currentFloor);
            System.out.println("Server port: " + port);

        } catch (IOException e) {
            System.err.println(
                "Server error: " + e.getMessage()
            );
        }
    }



public int getPort() {
    if (server == null) {
        throw new IllegalStateException(
            "Elevator server has not started"
        );
    }

    return server.getAddress().getPort();
}

public void stop() {
    if (server != null) {
        server.stop(0);
        server = null;
    }
}


    private synchronized void handleStatus(HttpExchange exchange)
            throws IOException {

        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405,
                "{\"error\":\"Method not allowed\"}");
            return;
        }

        String response = String.format(
            "{\"elevator\":\"%s\",\"currentFloor\":%d,\"status\":\"%s\"}",
            elevatorId, currentFloor, status
        );

        sendResponse(exchange, 200, response);
    }

    private synchronized void handleMove(HttpExchange exchange)
            throws IOException {

        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405,
                "{\"error\":\"Method not allowed\"}");
            return;
        }

        String body = new String(
            exchange.getRequestBody().readAllBytes(),
            StandardCharsets.UTF_8
        ).trim();

        int destination;

        try {
            destination = Integer.parseInt(body);
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400,
                "{\"error\":\"Invalid destination\"}");
            return;
        }

        if (destination < 1 || destination > numberOfFloors) {
            sendResponse(exchange, 400,
                "{\"error\":\"Floor out of range\"}");
            return;
        }

        // Initial simple simulation.
        if (destination > currentFloor) {
            status = "MOVING_UP";
        } else if (destination < currentFloor) {
            status = "MOVING_DOWN";
        }

        System.out.println(
            "Elevator " + elevatorId + ": "
            + currentFloor + " -> " + destination
        );

        currentFloor = destination;
        status = "IDLE";

        sendResponse(exchange, 200,
            "{\"result\":\"ARRIVED\",\"floor\":"
            + currentFloor + "}");
    }

    private void sendResponse(
            HttpExchange exchange,
            int code,
            String response) throws IOException {

        byte[] bytes =
            response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(code, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
