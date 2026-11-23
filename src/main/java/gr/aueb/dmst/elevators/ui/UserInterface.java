
package gr.aueb.dmst.elevators.ui;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserInterface {

    private static final int MIN_FLOOR = 1;
    private static final int MAX_FLOOR = 200;

    private final int installationFloor;
    private final String serverUrl;
    private final HttpClient client;

    public UserInterface(int floor, String url) {
        if (floor < MIN_FLOOR || floor > MAX_FLOOR) {
            throw new IllegalArgumentException(
                "Installation floor must be between 1 and 200"
            );
        }

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(
                "Server URL cannot be empty"
            );
        }

        this.installationFloor = floor;
        this.serverUrl = url.replaceAll("/+$", "");
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    }

    public static void main(String[] args) {
        int floor = 1;
        String server = "http://localhost:8080";

        try {
            for (String arg : args) {
                if (arg.startsWith("--floor=")) {
                    floor = Integer.parseInt(
                        arg.substring("--floor=".length())
                    );
                } else if (arg.startsWith("--server=")) {
                    server = arg.substring("--server=".length());
                } else {
                    throw new IllegalArgumentException(
                        "Unknown argument: " + arg
                    );
                }
            }

            UserInterface ui = new UserInterface(floor, server);
            ui.start();

        } catch (IllegalArgumentException e) {
            System.err.println("Configuration error: " + e.getMessage());
        }
    }

    public void start() {
        System.out.println("================================");
        System.out.println("       NEXUS SMART ELEVATORS");
        System.out.println("================================");
        System.out.println("Installation floor: " + installationFloor);
        System.out.println("Central Server: " + serverUrl);
        System.out.println("Type 'exit' to quit.");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("\nDestination floor: ");

                if (!scanner.hasNextLine()) {
                    break;
                }

                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                try {
                    int destination = Integer.parseInt(input);

                    if (destination < MIN_FLOOR
                            || destination > MAX_FLOOR) {
                        System.out.println(
                            "Invalid floor. Allowed range: 1-200."
                        );
                        continue;
                    }

                    if (destination == installationFloor) {
                        System.out.println(
                            "You are already on that floor."
                        );
                        continue;
                    }

                    requestElevator(destination);

                } catch (NumberFormatException e) {
                    System.out.println(
                        "Please enter a valid floor number."
                    );
                }
            }
        }
    }

    private void requestElevator(int destination) {
        String json = String.format(
            "{\"originFloor\":%d,\"destinationFloor\":%d}",
            installationFloor,
            destination
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(serverUrl + "/api/requests"))
            .timeout(Duration.ofSeconds(5))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        try {
            System.out.println("Sending request to Central Controller...");

            HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                String elevator = extractElevator(
                    response.body()
                );

                System.out.println("----------------------------");
                System.out.println("ASSIGNED ELEVATOR: " + elevator);
                System.out.println("From floor: " + installationFloor);
                System.out.println("To floor: " + destination);
                System.out.println("----------------------------");

            } else {
                System.out.println(
                    "Request rejected (HTTP "
                    + response.statusCode() + ")"
                );
                System.out.println(response.body());
            }

        } catch (IOException e) {
            System.out.println(
                "Cannot connect to Central Controller: "
                + e.getMessage()
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Request interrupted.");

        } catch (IllegalArgumentException e) {
            System.out.println(
                "Invalid server response: " + e.getMessage()
            );
        }
    }

    private static String extractElevator(String json) {
        Pattern pattern = Pattern.compile(
            "\"assignedElevator\"\\s*:\\s*\"([A-T])\""
        );

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new IllegalArgumentException(
                "Missing assignedElevator field"
            );
        }

        return matcher.group(1);
    }
}
