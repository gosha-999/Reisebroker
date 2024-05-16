import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TravelBroker {

    private static final int TRY_AGAINS = Integer.parseInt(Config.getProperty("attemptToTryAgain"));

    private static final int TRY_AGAIN_DELAY = Integer.parseInt(Config.getProperty("tryAgain_Delay"));

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



    //Logik zu Begin einer jeden Reisebuchung
    public synchronized void bookTravel(List<BookingRequest> requests) {
        Logger.info("TravelBroker", "Beginne mit der Buchung der Reise mit " + requests.size() + " Anfragen.");
        bookingResults.clear();
        successfullyBooked.clear();
        for (BookingRequest request : requests) {
            executorService.submit(() -> attemptBooking(request, 0)); // Parallelisieren der Buchungen
        }
    }

    //Senden von Buchungsanfragen
    private void attemptBooking(BookingRequest request, int attempt) {
        if (attempt >= TRY_AGAINS) {
            handleBookingError(request);
            return;
        }

        try {
            Logger.hotelRequest("TravelBroker", "Senden der Buchungsanfrage für Hotel " + request.getHotelId() + ", Zimmer: " + request.getNumberOfRooms() + ". Versuch: " + (attempt + 1));
            MessageBroker.getInstance().sendMessage(request, this, attempt, false);
        } catch (Exception e) {
            Logger.error("TravelBroker", "Fehler bei der Buchung für Hotel: " + request.getHotelId() + " beim Versuch " + (attempt + 1) + ". Fehler: " + e.getMessage());
            try {
                Thread.sleep(TRY_AGAIN_DELAY); // Verzögerung zwischen Wiederholungsversuchen
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            attemptBooking(request, attempt + 1);
        }
    }

    //Senden erneuter Buchungsanfragen (z.B. falls keine Antwort zurückkam)
    private void attemptConfirmation(BookingRequest request, int attempt) {
        if (attempt >= TRY_AGAINS) {
            handleBookingError(request);
            return;
        }

        try {
            Logger.debug("TravelBroker", "Anfordern der Bestätigung für Hotel " + request.getHotelId() + ", Zimmer: " + request.getNumberOfRooms() + ". Versuch: " + (attempt + 1));
            MessageBroker.getInstance().sendMessage(request, this, attempt, true);
        } catch (Exception e) {
            Logger.error("TravelBroker", "Fehler beim Anfordern der Bestätigung für Hotel: " + request.getHotelId() + " beim Versuch " + (attempt + 1) + ". Fehler: " + e.getMessage());
            try {
                Thread.sleep(TRY_AGAIN_DELAY); // Verzögerung zwischen Wiederholungsversuchen
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            attemptConfirmation(request, attempt + 1);
        }
    }

    //Logik zur Handhabung von Buchungsfehlern
    private void handleBookingError(BookingRequest failedRequest) {
        Logger.error("TravelBroker", "Beginne Rollback aufgrund eines Fehlers bei der Buchung für " + failedRequest.getHotelId());
        rollback();
        finalizeBooking(false);  // Finalize with rollback
    }


    //Logik des Rollbacks
    private void rollback() {
        for (BookingRequest request : successfullyBooked) {
            Logger.rollback("TravelBroker", "Führe Rollback für Buchung aus: " + request.getHotelId() + ", Zimmer: " + request.getNumberOfRooms());
            request.getService().cancelBooking(request.getHotelId(), request.getNumberOfRooms());
        }
        Logger.info("TravelBroker", "Rollback abgeschlossen.");
    }


    //Auswertung der erhaltenen Nachrichten
    public synchronized void receiveMessage(String message, BookingRequest request, int attempt, boolean isConfirmation) {
        if (message.startsWith("Fehler: Technischer Fehler")) {
            Logger.requestAgain("TravelBroker", "Erneuter Versuch für Hotel " + request.getHotelId() + " wegen technischem Fehler. Versuch: " + (attempt + 1));
            attemptBooking(request, attempt + 1);
        } else if (message.startsWith("Fehler: Fachlicher Fehler")) {
            if (isConfirmation) {
                Logger.requestAgain("TravelBroker", "Erneuter Versuch, Bestätigung für Hotel " + request.getHotelId() + " anzufordern. Versuch: " + (attempt + 1));
                attemptConfirmation(request, attempt + 1);
            } else {
                Logger.requestAgain("TravelBroker", "Fachlicher Fehler bei der Buchung für Hotel " + request.getHotelId() + ". Bestätigung wird erneut angefordert. Versuch: " + (attempt + 1));
                attemptConfirmation(request, attempt + 1);
            }
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


    //Gibt aus, ob die Reise-Buchung nun final erfolgreich war oder nicht, bei NEIN -> führe Rollback aus
    private void finalizeBooking(boolean allSuccessful) {
        if (allSuccessful) {
            Logger.info("TravelBroker", "Alle Buchungen der Reise erfolgreich abgeschlossen.");
        } else {
            Logger.error("TravelBroker", "Einige Buchungen der Reise fehlgeschlagen. Rollback wird eingeleitet.");
            rollback();
        }
    }
}
