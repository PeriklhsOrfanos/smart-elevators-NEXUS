
package gr.aueb.dmst.elevators.central;

public class DispatchAlgorithm {

    private final int[] elevatorFloors;
    private final int numberOfFloors;

    public DispatchAlgorithm(int numberOfElevators, int numberOfFloors) {

        if (numberOfElevators < 2 || numberOfElevators > 20) {
            throw new IllegalArgumentException(
                "Elevators must be between 2 and 20"
            );
        }

        if (numberOfFloors < 3 || numberOfFloors > 200) {
            throw new IllegalArgumentException(
                "Floors must be between 3 and 200"
            );
        }

        this.numberOfFloors = numberOfFloors;

        elevatorFloors = new int[numberOfElevators];

        // Initially all elevators are on floor 1
        for (int i = 0; i < numberOfElevators; i++) {
            elevatorFloors[i] = 1;
        }
    }

    public String assignElevator(int requestedFloor) {

        if (requestedFloor < 1 || requestedFloor > numberOfFloors) {
            throw new IllegalArgumentException("Invalid requested floor");
        }

        int bestElevator = 0;
        int shortestDistance = Integer.MAX_VALUE;

        for (int i = 0; i < elevatorFloors.length; i++) {

            int distance = Math.abs(
                elevatorFloors[i] - requestedFloor
            );

            if (distance < shortestDistance) {
                shortestDistance = distance;
                bestElevator = i;
            }
        }

        return String.valueOf((char) ('A' + bestElevator));
    }


public String assignElevator(
        int requestedFloor,
        boolean[] available) {

    if (requestedFloor < 1 || requestedFloor > numberOfFloors) {
        throw new IllegalArgumentException("Invalid requested floor");
    }

    if (available == null ||
            available.length != elevatorFloors.length) {
        throw new IllegalArgumentException(
            "Invalid availability array"
        );
    }

    int bestElevator = -1;
    int shortestDistance = Integer.MAX_VALUE;

    for (int i = 0; i < elevatorFloors.length; i++) {

        if (!available[i]) {
            continue;
        }

        int distance =
            Math.abs(elevatorFloors[i] - requestedFloor);

        if (distance < shortestDistance) {
            shortestDistance = distance;
            bestElevator = i;
        }
    }

    if (bestElevator == -1) {
        throw new IllegalStateException(
            "No available elevators"
        );
    }

    return String.valueOf((char) ('A' + bestElevator));
}


    public void updateElevatorFloor(char elevatorId, int floor) {

        int index = elevatorId - 'A';

        // Check elevator ID
        if (index < 0 || index >= elevatorFloors.length) {
            throw new IllegalArgumentException(
                "Invalid elevator ID"
            );
        }

        // Check building floor limits
        if (floor < 1 || floor > numberOfFloors) {
            throw new IllegalArgumentException(
                "Invalid floor"
            );
        }

        elevatorFloors[index] = floor;
    }
}
