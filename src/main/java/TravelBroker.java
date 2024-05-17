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
        Logger.info("TravelBroker", "Beginne mit der Buchung der Reise mit " + requests.size() + " Anfragen.");
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
            Logger.debug("TravelBroker", "Senden der Buchungsanfrage für Hotel " + request.getHotelId() + ", Zimmer: " + request.getNumberOfRooms() + ". Versuch: " + (attempt + 1));
            MessageBroker.getInstance().sendMessage(request, this, attempt, false);
        } catch (Exception e) {
            Logger.error("TravelBroker", "Fehler bei der Buchung für Hotel: " + request.getHotelId() + " beim Versuch " + (attempt + 1) + ". Fehler: " + e.getMessage());
            try {
                Thread.sleep(3000); // Verzögerung zwischen Wiederholungsversuchen
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            attemptBooking(request, attempt + 1);
        }
    }

    private void attemptConfirmation(BookingRequest request, int attempt) {
        if (attempt >= 3) {
            handleBookingError(request);
            return;
        }

        try {
            Logger.debug("TravelBroker", "Anfordern der Bestätigung für Hotel " + request.getHotelId() + ", Zimmer: " + request.getNumberOfRooms() + ". Versuch: " + (attempt + 1));
            MessageBroker.getInstance().sendMessage(request, this, attempt, true);
        } catch (Exception e) {
            Logger.error("TravelBroker", "Fehler beim Anfordern der Bestätigung für Hotel: " + request.getHotelId() + " beim Versuch " + (attempt + 1) + ". Fehler: " + e.getMessage());
            try {
                Thread.sleep(3000); // Verzögerung zwischen Wiederholungsversuchen
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            attemptConfirmation(request, attempt + 1);
        }
    }

    private void handleBookingError(BookingRequest failedRequest) {
        Logger.error("TravelBroker", "Beginne Rollback aufgrund eines Fehlers bei der Buchung für " + failedRequest.getHotelId());
        rollback();
        finalizeBooking(false);  // Finalize with rollback
    }

    private void rollback() {
        for (BookingRequest request : successfullyBooked) {
            Logger.debug("TravelBroker", "Führe Rollback für Buchung aus: " + request.getHotelId() + ", Zimmer: " + request.getNumberOfRooms());
            request.getService().cancelBooking(request.getHotelId(), request.getNumberOfRooms());
        }
        Logger.info("TravelBroker", "Rollback abgeschlossen.");
    }

    public synchronized void receiveMessage(String message, BookingRequest request, int attempt, boolean isConfirmation) {
        if (message.startsWith("Fehler: Technischer Fehler")) {
            Logger.info("TravelBroker", "Erneuter Versuch für Hotel " + request.getHotelId() + " wegen technischem Fehler. Versuch: " + (attempt + 1));
            attemptBooking(request, attempt + 1);
        } else if (message.startsWith("Fehler: Fachlicher Fehler") || message.startsWith("Fehler: Zimmer nicht verfügbar")) {
            Logger.error("TravelBroker", "Buchung für Hotel " + request.getHotelId() + " fehlgeschlagen: " + message);
            handleBookingError(request);
        } else {
            Logger.info("TravelBroker", "Erhaltene Nachricht: " + message);
            bookingResults.add(message);
            if (message.contains("erfolgreich")) {
                successfullyBooked.add(request);
            }
            if (bookingResults.size() == request.getService().getHotels().size()) {
                finalizeBooking(true);
            }
        }
    }

    private void finalizeBooking(boolean allSuccessful) {
        if (allSuccessful) {
            Logger.info("TravelBroker", "Alle Buchungen der Reise erfolgreich abgeschlossen.");
        } else {
            Logger.error("TravelBroker", "Einige Buchungen der Reise fehlgeschlagen. Rollback wird eingeleitet.");
            rollback();
        }
    }
}
