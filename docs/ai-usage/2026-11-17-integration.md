# NEXUS — REST Integration and Automated Tests

**Ημερομηνία:** 17/11/2026  
**AI Tool:** ChatGPT  
**AI Model:** GPT-6  
**Task:** Central–Elevator REST integration and automated integration testing

## Περιγραφή εργασίας

Σε αυτό το στάδιο υλοποιήθηκε η σύνδεση του Central Controller με τους ανεξάρτητους Elevator Controllers μέσω HTTP REST.

Ο Central Controller πλέον ανακτά την τρέχουσα θέση και κατάσταση των ανελκυστήρων, ενημερώνει τον Dispatch Algorithm και επιλέγει τον κοντινότερο διαθέσιμο ανελκυστήρα.

Στη συνέχεια στέλνει διαδοχικές εντολές μετακίνησης στον όροφο παραλαβής και στον προορισμό, επιστρέφοντας το αποτέλεσμα στο User Interface.

## Συνεισφορά AI

Το ChatGPT βοήθησε στον σχεδιασμό και στην παραγωγή κώδικα για:

- REST επικοινωνία μεταξύ Central και Elevator Controllers.
- Επιλογή ανελκυστήρα με βάση διαθεσιμότητα και απόσταση.
- Δυναμικές θύρες HTTP servers.
- Μεθόδους `getPort()` και `stop()`.
- Δημιουργία τεσσάρων JUnit integration tests.

## Ανθρώπινη συνεισφορά

Ανέλαβα τη δημιουργία και ενσωμάτωση των αρχείων στο υπάρχον Maven project, την προσαρμογή του κώδικα στο VS Code και τον έλεγχο των αλλαγών.

Επίσης, εκτέλεσα τις απαραίτητες δοκιμές μέσω WSL και Maven, επιβεβαιώνοντας ότι οι νέες λειτουργίες δεν προκαλούν αποτυχίες στους υπάρχοντες ελέγχους.

## Αποτελέσματα

- 6 Dispatch Algorithm unit tests.
- 4 Elevator Controller unit tests.
- 4 integration tests.
- 14 επιτυχημένα tests συνολικά.
- 0 failures, 0 errors.
- Maven BUILD SUCCESS.

## Περιορισμοί

Η μετακίνηση του ανελκυστήρα παραμένει στιγμιαία. Δεν έχει ολοκληρωθεί η προσομοίωση χρόνου κίνησης, η διαχείριση θυρών, η ουρά στάσεων ή η πλήρης διαχείριση ταυτόχρονων αιτημάτων.

Η επεξεργασία JSON χρειάζεται επίσης βελτίωση.

## AI Prompts and Responses

## AI Prompts and Responses

### Interaction 1 — Central Controller REST Integration

**User Prompt:**

> παμε να βελτιωσουμε τον `CentralController.java` ωστε
>
> 1. Να διαβάζει μέσω REST την τρέχουσα θέση κάθε ανελκυστήρα.
> 2. Να ενημερώνει τον `DispatchAlgorithm`.
> 3. Να επιλέγει τον κοντινότερο διαθέσιμο ανελκυστήρα.
> 4. Να στέλνει εντολή μετακίνησης στον επιλεγμένο Elevator Controller.
> 5. Να επιστρέφει στο UI το αποτέλεσμα της ανάθεσης.

**AI Response (Summary):**

Το ChatGPT πρότεινε την αναβάθμιση του `CentralController.java`, ώστε να επικοινωνεί με τους ανεξάρτητους Elevator Controllers μέσω HTTP REST.

Συγκεκριμένα, πρότεινε:

- Τη χρήση `HttpClient` της Java 17.
- Τη δημιουργία της μεθόδου `refreshElevatorPositions()`, η οποία καλεί το endpoint `GET /api/elevator/status` κάθε ανελκυστήρα.
- Την ενημέρωση του `DispatchAlgorithm` με τις πραγματικές θέσεις των ανελκυστήρων.
- Την προσθήκη υπερφορτωμένης μεθόδου `assignElevator(int requestedFloor, boolean[] available)`, για επιλογή μόνο μεταξύ διαθέσιμων ανελκυστήρων.
- Τη δημιουργία της `moveElevator()`, η οποία στέλνει HTTP POST στο `/api/elevator/move`.
- Την υλοποίηση διαδοχικής μετακίνησης προς τον όροφο παραλαβής και στη συνέχεια προς τον προορισμό.
- Την επιστροφή HTTP 503 όταν δεν υπάρχουν διαθέσιμοι ανελκυστήρες και HTTP 502 όταν αποτυγχάνει η επικοινωνία κατά τη μετακίνηση.

Επιπλέον, παρείχε πλήρη αναθεωρημένο κώδικα του `CentralController.java` και τις εντολές εκκίνησης τριών Elevator Controllers στις θύρες 8081–8083.

**Result:**

Υλοποιήθηκε η βασική REST επικοινωνία μεταξύ Central Controller και Elevator Controllers. Το αποτέλεσμα ελέγχθηκε χειροκίνητα μέσω WSL και εντολών `curl`.

---

### Interaction 2 — Automated JUnit Integration Tests

**User Prompt:**

> εκτελεσα τα τεστσ και λειτουργουν ολα σωστα. παμε να συνεχισουμε με **JUnit integration tests που εκκινούν ελεγχόμενα τους HTTP servers και ελέγχουν τη συνολική επικοινωνία**

**AI Response (Summary):**

Το ChatGPT πρότεινε τη δημιουργία αυτοματοποιημένων integration tests, τα οποία εκκινούν τους HTTP servers πριν από κάθε δοκιμή και τους τερματίζουν μετά την ολοκλήρωσή της.

Για τον σκοπό αυτό πρότεινε:

- Προσθήκη των μεθόδων `getPort()` και `stop()` στον `ElevatorController`.
- Υποστήριξη δυναμικών θυρών μέσω `start(0)`.
- Τροποποίηση του constructor του `CentralController`, ώστε να δέχεται πίνακα με τις θύρες των ανελκυστήρων.
- Προσθήκη των μεθόδων `start(int port)`, `getPort()` και `stop()` στον Central Controller.
- Χρήση των πραγματικών θυρών αντί για σταθερούς αριθμούς κατά την επικοινωνία REST.

Στη συνέχεια, παρείχε τον κώδικα του αρχείου `NexusIntegrationTest.java`, με τέσσερις ελέγχους:

1. `selectsClosestElevatorEndToEnd()` — Επιβεβαιώνει ότι επιλέγεται ο κοντινότερος διαθέσιμος ανελκυστήρας.
2. `updatesElevatorPositionAfterRequest()` — Ελέγχει ότι η τελική θέση του ανελκυστήρα ενημερώνεται σωστά.
3. `rejectsInvalidRequest()` — Ελέγχει την απόρριψη μη έγκυρων ορόφων με HTTP 400.
4. `returns503WhenElevatorsUnavailable()` — Ελέγχει ότι το σύστημα επιστρέφει HTTP 503 όταν όλοι οι ανελκυστήρες είναι εκτός λειτουργίας.

Το ChatGPT πρότεινε επίσης τη διαδοχική εκτέλεση του Maven build δύο φορές, ώστε να ελεγχθεί αν οι servers αποδεσμεύουν σωστά τις θύρες τους.

**Result:**

Δημιουργήθηκε η υποδομή αυτοματοποιημένων integration tests με χρήση JUnit 5, Java HttpClient και δυναμικών θυρών.

---

## Token Usage

Οι ακριβείς αριθμοί tokens δεν είναι διαθέσιμοι από τη συγκεκριμένη συνομιλία και δεν καταχωρίζονται εκτιμήσεις.