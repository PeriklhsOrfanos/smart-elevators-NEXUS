
package gr.aueb.dmst.elevators.elevator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ElevatorControllerTest {

    @Test
    void createsValidElevator() {
        assertDoesNotThrow(
            () -> new ElevatorController('A', 1, 10)
        );
    }

    @Test
    void rejectsInvalidElevatorId() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new ElevatorController('Z', 1, 10)
        );
    }

    @Test
    void rejectsFloorBelowMinimum() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new ElevatorController('A', 0, 10)
        );
    }

    @Test
    void rejectsFloorAboveMaximum() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new ElevatorController('B', 15, 10)
        );
    }
}
