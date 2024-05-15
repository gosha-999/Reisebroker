import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TravelBroker {
    private static TravelBroker instance = new TravelBroker();
    private List<String> bookingResults;
    private List<BookingRequest> successfullyBooked;
    private ExecutorService executorService;

    private TravelBroker() {
        this.bookingResults = new ArrayList<>();
        this.successfullyBooked = new ArrayList<>();
        this.executorService = Executors.newCachedThreadPool(); // Erstellen eines Thread-Pools für parallele Ausführung
    }

    public static TravelBroker getInstance() {
        return instance;
    }

    public synchronized void bookTravel(List<BookingRequest> requests) {
        Logger.info("Beginne mit der Buchung der Reise mit " + requests.size() + " Anfragen.");
        bookingResults.clear();
        successfullyBooked.clear();
        for (BookingRequest request : requests) {
            executorService.submit(() -> attemptBooking(request, 0)); // Parallelisieren der Buchungen
        }
    }

    private void attemptBooking(BookingRequest request, int attempt) {
        if (attempt >= 3) {
            handleBookingError(request);
            return;
        }

        try {
            Logger.debug("Senden der Buchungsanfrage für Hotel " + request.getHotelId() + ", Zimmer: " + request.getNumberOfRooms() + ". Versuch: " + (attempt + 1));
            MessageBroker.getInstance().sendMessage(request, this);
            successfullyBooked.add(request);
        } catch (Exception e) {
            Logger.error("Fehler bei der Buchung für Hotel: " + request.getHotelId() + " beim Versuch " + (attempt + 1) + ". Fehler: " + e.getMessage());
            try {
                Thread.sleep(3000); // Verzögerung zwischen Wiederholungsversuchen
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            attemptBooking(request, attempt + 1);
        }
    }

    private void handleBookingError(BookingRequest failedRequest) {
        Logger.error("Beginne Rollback aufgrund eines Fehlers bei der Buchung für " + failedRequest.getHotelId());
        rollback();
    }

    private void rollback() {
        for (BookingRequest request : successfullyBooked) {
            Logger.debug("Führe Rollback für Buchung aus: " + request.getHotelId() + ", Zimmer: " + request.getNumberOfRooms());
            request.getService().cancelBooking(request.getHotelId(), request.getNumberOfRooms());
        }
        Logger.info("Rollback abgeschlossen.");
    }

    public synchronized void receiveMessage(String message) {
        Logger.info("Erhaltene Nachricht: " + message);
        bookingResults.add(message);
        if (bookingResults.size() == successfullyBooked.size()) {
            finalizeBooking();
        }
    }

    private void finalizeBooking() {
        boolean allSuccessful = bookingResults.stream().allMatch(result -> result.contains("erfolgreich"));
        if (allSuccessful) {
            Logger.info("Alle Buchungen erfolgreich abgeschlossen.");
        } else {
            Logger.error("Einige Buchungen fehlgeschlagen. Rollback wird eingeleitet.");
            rollback();
        }
        bookingResults.forEach(Logger::info);
    }
}
