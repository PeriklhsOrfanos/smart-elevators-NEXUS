
package gr.aueb.dmst.elevators.integration;

import gr.aueb.dmst.elevators.central.CentralController;
import gr.aueb.dmst.elevators.elevator.ElevatorController;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class NexusIntegrationTest {

    private ElevatorController elevatorA;
    private ElevatorController elevatorB;
    private CentralController central;
    private HttpClient client;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        // Each test gets fresh elevator state.
        elevatorA = new ElevatorController('A', 1, 10);
        elevatorB = new ElevatorController('B', 6, 10);

        // Port 0 asks the OS for a free port.
        elevatorA.start(0);
        elevatorB.start(0);

        central = new CentralController(
            2,
            10,
            new int[] {
                elevatorA.getPort(),
                elevatorB.getPort()
            }
        );

        central.start(0);

        client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

        baseUrl = "http://localhost:" + central.getPort();
    }

    @AfterEach
    void tearDown() {
        if (central != null) {
            central.stop();
        }

        if (elevatorA != null) {
            elevatorA.stop();
        }

        if (elevatorB != null) {
            elevatorB.stop();
        }
    }

    private HttpResponse<String> sendRequest(
            int origin,
            int destination)
            throws IOException, InterruptedException {

        String json = String.format(
            "{\"originFloor\":%d,\"destinationFloor\":%d}",
            origin,
            destination
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/requests"))
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        return client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );
    }

    private HttpResponse<String> getElevatorStatus(int port)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(
                "http://localhost:" + port + "/api/elevator/status"
            ))
            .timeout(Duration.ofSeconds(3))
            .GET()
            .build();

        return client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );
    }

    @Test
    void selectsClosestElevatorEndToEnd()
            throws Exception {

        // A starts at 1, B at 6.
        // For origin 7, B is closer.
        HttpResponse<String> response =
            sendRequest(7, 3);

        assertEquals(200, response.statusCode());

        assertTrue(
            response.body().contains(
                "\"assignedElevator\":\"B\""
            )
        );

        assertTrue(
            response.body().contains(
                "\"result\":\"ARRIVED\""
            )
        );
    }

    @Test
    void updatesElevatorPositionAfterRequest()
            throws Exception {

        HttpResponse<String> response =
            sendRequest(7, 3);

        assertEquals(200, response.statusCode());

        HttpResponse<String> status =
            getElevatorStatus(elevatorB.getPort());

        assertEquals(200, status.statusCode());

        assertTrue(
            status.body().contains(
                "\"currentFloor\":3"
            )
        );

        assertTrue(
            status.body().contains(
                "\"status\":\"IDLE\""
            )
        );
    }

    @Test
    void rejectsInvalidRequest()
            throws Exception {

        HttpResponse<String> response =
            sendRequest(7, 250);

        assertEquals(400, response.statusCode());

        assertTrue(
            response.body().contains(
                "\"error\":\"Invalid floor\""
            )
        );
    }

    @Test
    void returns503WhenElevatorsUnavailable()
            throws Exception {

        elevatorA.stop();
        elevatorB.stop();

        HttpResponse<String> response =
            sendRequest(7, 3);

        assertEquals(503, response.statusCode());

        assertTrue(
            response.body().contains(
                "\"error\":\"No available elevators\""
            )
        );
    }
}
