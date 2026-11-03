import java.util.Scanner;

public class UserInterface {
    private static int currentFloor=0;
    private static String serverUrl = "http://localhost:8080";
    private static final int MIN_FLOOR = 1;
    private static final int MAX_FLOOR = 200;

    public static void main(String[]args) {
        parseCommandLineArguments(args);
        System.out.println("=== Διεπαφή Χρήστη Ανελκυστήρα ===");
        System.out.println("Τρέχων όροφος: " + currentFloor);
        System.out.println("Σύνδεση με τον διακομιστή: " + serverUrl);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Πληκτρολογήστε όροφο προορισμού ή 'exit' για έξοδο: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Έξοδος από τη διεπαφή χρήστη.");
                break;
            }

            try {
                int destinationFloor = Integer.parseInt(input);

                
                if (destinationFloor == currentFloor) {
                    System.out.println(" Βρίσκεστε ήδη στον όροφο " + currentFloor);
                    continue;
                }

               if (!isValidFloor(destinationFloor)) {
                    continue;
                }

                System.out.println("-> Αποστολή αιτήματος από τον όροφο " + currentFloor + " στον όροφο " + destinationFloor + "..."); 
                 }        
                catch (NumberFormatException e) {
                    System.out.println(" Σφάλμα: Παρακαλώ εισάγετε έναν έγκυρο αριθμό ορόφου.");
                }

        scanner.close();
        }
        private static void parseCommandLineArguments(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--floor=")) {
                try {
                    currentFloor = Integer.parseInt(arg.substring(8));
                } catch (NumberFormatException e) {
                    System.err.println("⚠️ Μη έγκυρος όροφος στις παραμέτρους. Χρήση προεπιλογής (0).");
                }
            } else if (arg.startsWith("--server=")) {
                serverUrl = arg.substring(9);
            }
        }
    }
}   
private static boolean isValidFloor(int floor) {
        if (floor < MIN_FLOOR || floor > MAX_FLOOR) {
            System.out.println(" Σφάλμα: Ο όροφος πρέπει να είναι μεταξύ " + MIN_FLOOR + " και " + MAX_FLOOR + ".");
            return false;
        }
        return true;
    }


    