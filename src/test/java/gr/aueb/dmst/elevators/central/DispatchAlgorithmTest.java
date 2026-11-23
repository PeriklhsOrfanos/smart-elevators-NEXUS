
package gr.aueb.dmst.elevators.central;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DispatchAlgorithmTest {

    @Test
    void selectsClosestElevator() {
        DispatchAlgorithm algorithm = new DispatchAlgorithm(4);

        algorithm.updateElevatorFloor('A', 1);
        algorithm.updateElevatorFloor('B', 4);
        algorithm.updateElevatorFloor('C', 9);
        algorithm.updateElevatorFloor('D', 2);

        String result = algorithm.assignElevator(7);

        assertEquals("C", result);
    }

    @Test
    void selectsFirstElevatorOnTie() {
        DispatchAlgorithm algorithm = new DispatchAlgorithm(3);

        String result = algorithm.assignElevator(5);

        assertEquals("A", result);
    }

    @Test
    void rejectsInvalidElevatorId() {
        DispatchAlgorithm algorithm = new DispatchAlgorithm(3);

        assertThrows(
            IllegalArgumentException.class,
            () -> algorithm.updateElevatorFloor('Z', 5)
        );
    }

    @Test
    void rejectsInvalidNumberOfElevators() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new DispatchAlgorithm(1)
        );
    }
}
