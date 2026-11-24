# NEXUS — AI Usage Documentation

## 1. Γενικές πληροφορίες

**Ημερομηνία:** 14/11/2026

**Εργαλείο:** ChatGPT (OpenAI)

**Μοντέλο:** GPT-6

**Αντικείμενο:** Ανάπτυξη Central Controller, Dispatch Algorithm, User Interface και JUnit tests.

## 2. Χρήση τεχνητής νοημοσύνης

Χρησιμοποιήθηκε το ChatGPT για την παραγωγή αρχικού κώδικα, την υποστήριξη σχεδιαστικών αποφάσεων και την επίλυση τεχνικών προβλημάτων.

Συγκεκριμένα, το AI βοήθησε στα εξής:

- Δημιουργία και βελτίωση του CentralController.java.
- Υλοποίηση ενός αρχικού αλγορίθμου επιλογής του κοντινότερου ανελκυστήρα.
- Αναδιάρθρωση του UserInterface.java και προσθήκη πραγματικής REST επικοινωνίας.
- Δημιουργία τεσσάρων JUnit tests.
- Επεξήγηση σφαλμάτων Java και Maven.

## 3. Ανθρώπινη συνεισφορά

Η δική μου συμμετοχή περιλάμβανε:

- Οργάνωση του repository σε ξεχωριστό integration branch.
- Εγκατάσταση και διαμόρφωση Java 17 και Maven στο WSL.
- Δημιουργία των packages και ενσωμάτωση των Java αρχείων.
- Διόρθωση σφάλματος μεταγλώττισης στο multi-catch, με καθοδήγηση AI.
- Εντοπισμό και διόρθωση της διαδρομής του αρχείου JUnit, με καθοδήγηση AI.
- Εκτέλεση Maven builds και έλεγχο των αποτελεσμάτων.
- Δοκιμές HTTP endpoints μέσω curl.
- Χειροκίνητες δοκιμές του UI με διαφορετικές εισόδους.
- Έλεγχο της επικοινωνίας μεταξύ UI και Central Controller.

Η αρχική υλοποίηση της διεπαφής χρήστη και του Central Controller βασίστηκε επίσης σε προϋπάρχοντα αρχεία της ομάδας.

## 4. Αποτελέσματα

Επιβεβαιώθηκαν τα ακόλουθα:

- Επιτυχής μεταγλώττιση του Maven project.
- Τέσσερα επιτυχημένα JUnit tests.
- HTTP 200 για έγκυρο αίτημα.
- HTTP 400 για αίτημα με μη έγκυρο όροφο.
- Επιτυχής επικοινωνία του CLI User Interface με τον Central Controller.
- Σωστός χειρισμός μη αριθμητικής εισόδου και εντολής exit.

## 5. Περιορισμοί

Δεν έχουν ολοκληρωθεί ακόμη η επικοινωνία με τους Elevator Controllers, η αυτόματη ενημέρωση θέσεων ανελκυστήρων και η τελική τυποποίηση του REST API.

## 6. Κατανάλωση AI Tokens

Οι ακριβείς μετρήσεις input, output, cached και reasoning tokens δεν είναι διαθέσιμες από τη συγκεκριμένη συνομιλία.

Δεν χρησιμοποιήθηκαν εκτιμώμενοι ή επινοημένοι αριθμοί.

## 7. Πλήρες ιστορικό προτροπών και απαντήσεων


# NEXUS — AI Usage Log

## Session: 2026-11-23

### Tool
ChatGPT

### Model
GPT-6

### Development tasks
- Maven project organization
- CentralController REST implementation
- DispatchAlgorithm implementation
- CLI UserInterface integration
- JUnit test generation
- Compilation error debugging

### Human contributions
- Created and organized the repository structure
- Configured Java 17 and Maven in WSL
- Integrated and reviewed the proposed Java files
- Corrected compilation and test-directory issues
- Executed Maven builds and JUnit tests
- Tested REST endpoints using curl
- Tested CLI input and server responses

### Verified results
- Maven BUILD SUCCESS
- 4 JUnit tests passed
- REST health endpoint returned HTTP 200
- Valid dispatch request returned HTTP 200
- Invalid floor request returned HTTP 400
- CLI successfully communicated with CentralController

### Prompts and responses
The complete prompts and AI responses from this
development session must be preserved here
or in an accompanying conversation export.

### Token usage
Exact token counts are not available
from the current ChatGPT conversation.

No token counts have been estimated or invented.

### Limitations
- Elevator movement is not yet integrated
- Elevator positions are not updated automatically
- The JSON parsing implementation is preliminary
- The REST API requires further standardization
