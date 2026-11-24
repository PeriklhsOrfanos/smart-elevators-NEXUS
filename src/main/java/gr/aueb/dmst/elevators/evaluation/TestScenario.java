package gr.aueb.dmst.elevators.evaluation;

import java.util.ArrayList;
import java.util.List;


public class TestScenario {
    private final String scenarioName;
    private final List<int[]> requests; 

    public TestScenario(String scenarioName) {
        this.scenarioName = scenarioName;
        this.requests = new ArrayList<>();
    }

    public void addRequest(int pickupFloor, int destinationFloor) {
        if (pickupFloor < 0 || destinationFloor < 0 || pickupFloor == destinationFloor) {
            throw new IllegalArgumentException("Μη έγκυροι όροφοι αιτήματος.");
        }
        requests.add(new int[]{pickupFloor, destinationFloor});
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public List<int[]> getRequests() {
        return new ArrayList<>(requests);
    }

    public int getTotalRequests() {
        return requests.size();
    }
}
