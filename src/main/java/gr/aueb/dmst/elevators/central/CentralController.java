
package gr.aueb.dmst.elevators.central;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CentralController {

    private final int numberOfElevators;
    private final int numberOfFloors;
    private final DispatchAlgorithm dispatchAlgorithm;
    private final int[] elevatorPorts;
    private final HttpClient client;

    private HttpServer server;

    private static final int CENTRAL_PORT = 8080;
    private static final int ELEVATOR_BASE_PORT = 8081;

    
public CentralController(int elevators, int floors) {
    this(elevators, floors, defaultElevatorPorts(elevators));
}

public CentralController(
        int elevators,
        int floors,
        int[] elevatorPorts) {

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

    if (elevatorPorts == null ||
            elevatorPorts.length != elevators) {
        throw new IllegalArgumentException(
            "Invalid elevator port configuration"
        );
    }

    for (int port : elevatorPorts) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                "Invalid elevator port"
            );
        }
    }

    this.numberOfElevators = elevators;
    this.numberOfFloors = floors;
    this.elevatorPorts = elevatorPorts.clone();

    this.dispatchAlgorithm =
        new DispatchAlgorithm(elevators, floors);

    this.client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(2))
        .build();
}

private static int[] defaultElevatorPorts(int elevators) {
    if (elevators < 2 || elevators > 20) {
        throw new IllegalArgumentException(
            "Elevators must be between 2 and 20"
        );
    }

    int[] ports = new int[elevators];

    for (int i = 0; i < elevators; i++) {
        ports[i] = 8081 + i;
    }

    return ports;
}

public void start() {
    start(8080);
}
    public void start(int port) {

        try {
            server = HttpServer.create(
    new InetSocketAddress(port), 0
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
            System.out.println("Port: " + server.getAddress().getPort());

        } catch (IOException e) {
            System.err.println(
                "Failed to start server: " + e.getMessage()
            );
        }
    }


public int getPort() {
    if (server == null) {
        throw new IllegalStateException(
            "Central server has not started"
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


    private void handleHealth(HttpExchange exchange)
            throws IOException {

        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405,
                "{\"error\":\"Method not allowed\"}");
            return;
        }

        sendResponse(exchange, 200, "{\"status\":\"UP\"}");
    }

    private synchronized void handleRequest(HttpExchange exchange)
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

        int originFloor;
        int destinationFloor;

        try {
            originFloor = readInteger(body, "originFloor");
            destinationFloor =
                readInteger(body, "destinationFloor");

        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400,
                "{\"error\":\"Invalid request data\"}");
            return;
        }

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

        // Refresh positions of reachable, idle elevators.
        boolean[] available = refreshElevatorPositions();

        String assignedElevator;

        try {
            assignedElevator =
                dispatchAlgorithm.assignElevator(
                    originFloor, available
                );
        } catch (IllegalStateException e) {
            sendResponse(exchange, 503,
                "{\"error\":\"No available elevators\"}");
            return;
        }

        char elevatorId = assignedElevator.charAt(0);

        // First pick up the passenger.
        boolean pickupSuccess =
            moveElevator(elevatorId, originFloor);

        if (!pickupSuccess) {
            sendResponse(exchange, 502,
                "{\"error\":\"Elevator pickup failed\"}");
            return;
        }

        dispatchAlgorithm.updateElevatorFloor(
            elevatorId, originFloor
        );

        // Then travel to the requested destination.
        boolean destinationSuccess =
            moveElevator(elevatorId, destinationFloor);

        if (!destinationSuccess) {
            sendResponse(exchange, 502,
                "{\"error\":\"Elevator destination move failed\"}");
            return;
        }

        dispatchAlgorithm.updateElevatorFloor(
            elevatorId, destinationFloor
        );

        String response = String.format(
            "{\"assignedElevator\":\"%s\","
            + "\"originFloor\":%d,"
            + "\"destinationFloor\":%d,"
            + "\"result\":\"ARRIVED\"}",
            assignedElevator,
            originFloor,
            destinationFloor
        );

        System.out.println(
            "Request " + originFloor + " -> "
            + destinationFloor
            + " | Elevator " + assignedElevator
            + " | ARRIVED"
        );

        sendResponse(exchange, 200, response);
    }

    private boolean[] refreshElevatorPositions() {

        boolean[] available = new boolean[numberOfElevators];

        for (int i = 0; i < numberOfElevators; i++) {

            char id = (char) ('A' + i);
            int port = elevatorPorts[i];

            String url = "http://localhost:" + port
                + "/api/elevator/status";

            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

                HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() != 200) {
                    continue;
                }

                String json = response.body();

                String reportedId =
                    readString(json, "elevator");

                int floor =
                    readInteger(json, "currentFloor");

                String status =
                    readString(json, "status");

                // Reject inconsistent or busy elevators.
                if (!reportedId.equals(String.valueOf(id))
                        || floor < 1
                        || floor > numberOfFloors
                        || !"IDLE".equals(status)) {
                    continue;
                }

                dispatchAlgorithm.updateElevatorFloor(
                    id, floor
                );

                available[i] = true;

                System.out.println(
                    "Elevator " + id
                    + ": floor " + floor
                    + " | " + status
                );

            } catch (IOException | IllegalArgumentException e) {
                System.err.println(
                    "Elevator " + id + " unavailable: "
                    + e.getMessage()
                );

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return available;
    }

    private boolean moveElevator(char id, int destination) {

        int port = elevatorPorts[id - 'A'];

        String url = "http://localhost:" + port
            + "/api/elevator/move";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(3))
                .header("Content-Type", "text/plain")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        String.valueOf(destination)
                    )
                )
                .build();

            HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                System.err.println(
                    "Move rejected: HTTP "
                    + response.statusCode()
                );
                return false;
            }

            String result =
                readString(response.body(), "result");

            int reportedFloor =
                readInteger(response.body(), "floor");

            return "ARRIVED".equals(result)
                && reportedFloor == destination;

        } catch (IOException | IllegalArgumentException e) {
            System.err.println(
                "Movement error: " + e.getMessage()
            );
            return false;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static int readInteger(String json, String field) {
        Pattern pattern = Pattern.compile(
            "\"" + field + "\"\\s*:\\s*(-?\\d+)"
        );

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new IllegalArgumentException(
                "Missing integer: " + field
            );
        }

        return Integer.parseInt(matcher.group(1));
    }

    private static String readString(String json, String field) {
        Pattern pattern = Pattern.compile(
            "\"" + field + "\"\\s*:\\s*\"([^\"]+)\""
        );

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new IllegalArgumentException(
                "Missing string: " + field
            );
        }

        return matcher.group(1);
    }

    private static void sendResponse(
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

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
