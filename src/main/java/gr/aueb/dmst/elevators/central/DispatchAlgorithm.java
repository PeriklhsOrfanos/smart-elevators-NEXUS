
package gr.aueb.dmst.elevators.central;

public class DispatchAlgorithm {

    private final int[] elevatorFloors;

    public DispatchAlgorithm(int numberOfElevators) {
        if (numberOfElevators < 2 || numberOfElevators > 20) {
            throw new IllegalArgumentException(
                "Elevators must be between 2 and 20"
            );
        }

        elevatorFloors = new int[numberOfElevators];

        // Initial position: all elevators on floor 1.
        for (int i = 0; i < numberOfElevators; i++) {
            elevatorFloors[i] = 1;
        }
    }

    public String assignElevator(int requestedFloor) {
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

    public void updateElevatorFloor(char elevatorId, int floor) {
        int index = elevatorId - 'A';

        if (index < 0 || index >= elevatorFloors.length) {
            throw new IllegalArgumentException(
                "Invalid elevator ID"
            );
        }

        elevatorFloors[index] = floor;
    }
}
