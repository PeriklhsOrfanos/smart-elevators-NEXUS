package gr.aueb.dmst.elevators;

public class CentralController {
    private int numberOfElevators;
    private int numberOfFloors;

    public CentralController(int numberOfElevators, int numberOfFloors) {
        this.numberOfElevators = numberOfElevators;
        this.numberOfFloors = numberOfFloors;
    }

    public static void main(String[] args) {
        System.out.println("Central Control Module Starting...");
        CentralController controller = new CentralController(4, 10);
        controller.start();
    }

    public void start() {
        System.out.println("Central Control active for " 
            + numberOfElevators + " elevators and " 
            + numberOfFloors + " floors.");
    }
}
