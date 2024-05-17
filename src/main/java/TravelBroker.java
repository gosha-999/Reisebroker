import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TravelBroker {
    private static TravelBroker instance = new TravelBroker();
    private ExecutorService executorService;

    private static final int TRYAGAIN_DELAY = Integer.parseInt(Config.getProperty("tryAgain_Delay"));
    private static final int ATTEMPT_TO_TRY_AGAIN = Integer.parseInt(Config.getProperty("attemptToTryAgain"));

    private TravelBroker() {
        this.executorService = Executors.newCachedThreadPool(); // Erstellen eines Thread-Pools für parallele Ausführung
    }

    public static TravelBroker getInstance() {
        return instance;
    }

    public synchronized void bookTravel(List<BookingRequest> requests) {
        BookingContext context = new BookingContext(requests);
        Logger.info("TravelBroker", "Beginne Buchung der Reise mit " + requests.size() + " Anfragen.");
        for (BookingRequest request : requests) {
            request.setContext(context);  // Setzen des Kontextes
            executorService.submit(() -> attemptBooking(request, 0, context)); // Parallelisieren der Buchungen
        }
    }

    private void attemptBooking(BookingRequest request, int attempt, BookingContext context) {
        if (attempt >= ATTEMPT_TO_TRY_AGAIN) {
            handleBookingError(request, context);
            return;
        }

        try {
            Logger.debug("TravelBroker", "Sende Buchungsanfrage für Hotel " + request.getHotelId() + ", Zimmer: " + request.getNumberOfRooms() + ". Versuch: " + (attempt + 1));
            MessageBroker.getInstance().sendMessage(request, context, attempt, false);
        } catch (Exception e) {
            Logger.error("TravelBroker", "Fehler bei der Buchung für Hotel " + request.getHotelId() + " beim Versuch " + (attempt + 1) + ". Fehler: " + e.getMessage());
            try {
                Thread.sleep(TRYAGAIN_DELAY); // Verzögerung zwischen Wiederholungsversuchen
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            attemptBooking(request, attempt + 1, context);
        }
    }

    private void attemptConfirmation(BookingRequest request, int attempt, BookingContext context) {
        if (attempt >= ATTEMPT_TO_TRY_AGAIN) {
            handleBookingError(request, context);
            return;
        }

        try {
            Logger.debug("TravelBroker", "Erneuter Versuch für Bestätigung der Buchung für Hotel " + request.getHotelId() + ", Zimmer: " + request.getNumberOfRooms() + ". Versuch: " + (attempt + 1));
            MessageBroker.getInstance().sendMessage(request, context, attempt, true);
        } catch (Exception e) {
            Logger.error("TravelBroker", "Fehler bei der Bestätigung der Buchung für Hotel " + request.getHotelId() + " beim Versuch " + (attempt + 1) + ". Fehler: " + e.getMessage());
            try {
                Thread.sleep(TRYAGAIN_DELAY); // Verzögerung zwischen Wiederholungsversuchen
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            attemptConfirmation(request, attempt + 1, context);
        }
    }

    private synchronized void handleBookingError(BookingRequest failedRequest, BookingContext context) {
        context.setRollbackInitiated(true);
        Logger.error("TravelBroker", "Fehler bei der Buchung für " + failedRequest.getHotelId() + ". Rollback wird vorbereitet.");
        context.incrementCompletedRequests();
        checkAndHandleCompletion(context);
    }

    private synchronized void rollback(BookingContext context) {
        Logger.debug("TravelBroker", "Rollback gestartet.");
        for (BookingRequest request : context.getSuccessfullyBooked()) {
            Logger.debug(Logger.YELLOW + "TravelBroker", "Führe Rollback für Buchung aus: " + request.getHotelId() + ", Zimmer: " + request.getNumberOfRooms() + Logger.RESET);
            request.getService().cancelBooking(request.getHotelId(), request.getNumberOfRooms());
        }
        Logger.info(Logger.YELLOW + "TravelBroker", "Rollback abgeschlossen." + Logger.RESET);
    }

    public synchronized void receiveMessage(String message, BookingRequest request, int attempt, BookingContext context, boolean isConfirmation) {
        if (message.equals("Fehler: Technischer Fehler")) {
            Logger.info(Logger.RED + "TravelBroker", "Erneuter Versuch für Hotel " + request.getHotelId() + " wegen technischem Fehler. Versuch: " + (attempt + 1));
            attemptBooking(request, attempt + 1, context);
        } else if (message.equals("Fehler: Fachlicher Fehler")) {
            Logger.error(Logger.RED + "TravelBroker", "Bestätigung für Buchung im Hotel " + request.getHotelId() + " fehlgeschlagen: " + message);
            attemptConfirmation(request, attempt + 1, context);
        } else if (message.equals("Fehler: Zimmer nicht verfügbar")) {
            Logger.error(Logger.RED + "TravelBroker", "Zimmer im Hotel " + request.getHotelId() + " nicht verfügbar: " + message);
            context.setRollbackInitiated(true); // Markieren des Rollback-Zustands
            context.incrementCompletedRequests();
            checkAndHandleCompletion(context);
        } else {
            Logger.info("TravelBroker", "Erhaltene Nachricht: " + message);
            context.addBookingResult(message);
            if (message.contains("Buchung erfolgreich")) {
                context.addSuccessfulBooking(request);
            }
            context.incrementCompletedRequests();
            checkAndHandleCompletion(context);
        }
    }

    private synchronized void checkAndHandleCompletion(BookingContext context) {
        if (context.getCompletedRequests() == context.getTotalRequests()) {
            if (context.isRollbackInitiated()) {
                rollback(context);
            } else {
                finalizeBooking(true, context);
            }
        }
    }

    private synchronized void finalizeBooking(boolean allSuccessful, BookingContext context) {
        if (context.isRollbackInitiated()) {
            return;
        }

        if (allSuccessful) {
            Logger.info(Logger.GREEN+"TravelBroker", "Alle Buchungen der Reise erfolgreich abgeschlossen.");
        } else {
            Logger.error(Logger.RED + "TravelBroker", "Einige Buchungen der Reise fehlgeschlagen. Rollback wird eingeleitet.");
            rollback(context);
        }
    }
}
