import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HotelBookingService {
    private String name;
    private Map<String, Hotel> hotels;
    private ExecutorService executorService;
    private static final double TECHNICAL_FAILURE_PROBABILITY = 0.1; // Beispielwert für technischen Fehler
    private static final double BUSINESS_FAILURE_PROBABILITY = 0.2; // Beispielwert für fachlichen Fehler

    public HotelBookingService() {
        this.hotels = new HashMap<>();
        this.executorService = Executors.newCachedThreadPool(); // Thread-Pool zur parallelen Verarbeitung
    }

    public void addHotel(Hotel hotel) {
        hotels.put(hotel.getId(), hotel);
    }

    public Collection<Hotel> getHotels() {
        return hotels.values();
    }

    public boolean hasHotel(Hotel hotel) {
        return hotels.containsKey(hotel.getId());
    }

    public void processBookingRequest(BookingRequest request, int attempt, boolean isConfirmation) {
        executorService.submit(() -> {
            try {
                String response;
                if (isConfirmation) {
                    response = confirmBooking(request.getHotelId(), request.getNumberOfRooms());
                } else {
                    response = bookHotelRooms(request.getHotelId(), request.getNumberOfRooms());
                }
                Logger.debug("HotelBookingService", "Antwort an MessageBroker für Hotel " + request.getHotelId() + ": " + response);
                MessageBroker.getInstance().sendResponse(response, request, TravelBroker.getInstance(), attempt, request.getContext(), isConfirmation);
            } catch (Exception e) {
                Logger.debug("HotelBookingService", "Fehler beim Verarbeiten der Buchung für Hotel " + request.getHotelId() + ". Fehler: " + e.getMessage());
                MessageBroker.getInstance().sendResponse("Fehler: Technischer Fehler", request, TravelBroker.getInstance(), attempt, request.getContext(), isConfirmation);
            }
        });
    }

    private synchronized String bookHotelRooms(String hotelId, int numberOfRooms) throws Exception {
        if (new Random().nextDouble() < TECHNICAL_FAILURE_PROBABILITY) {
            throw new Exception("Technischer Fehler bei der Buchung von Hotel " + hotelId);
        }
        Hotel hotel = hotels.get(hotelId);
        if (hotel == null) {
            throw new Exception("Hotel nicht gefunden: " + hotelId);
        }
        if (hotel.bookRooms(numberOfRooms)) {
            if (new Random().nextDouble() < BUSINESS_FAILURE_PROBABILITY) {
                // Fachlicher Fehler: Buchung erfolgreich, aber keine Bestätigung gesendet
                throw new Exception("Fachlicher Fehler: Buchungsbestätigung nicht übermittelt");
            }
            Logger.info("HotelBookingService", "Buchung erfolgreich für " + numberOfRooms + " Zimmer im Hotel " + hotel.getName() + ". Verbleibende Zimmer: " + hotel.getAvailableRooms());
            return "Buchung erfolgreich für " + numberOfRooms + " Zimmer im Hotel " + hotel.getName();
        } else {
            Logger.info("HotelBookingService", "Zimmer im Hotel " + hotelId + " nicht verfügbar.");
            return "Fehler: Zimmer nicht verfügbar";
        }
    }

    private synchronized String confirmBooking(String hotelId, int numberOfRooms) throws Exception {
        if (new Random().nextDouble() < TECHNICAL_FAILURE_PROBABILITY) {
            throw new Exception("Technischer Fehler bei der Bestätigung von Hotel " + hotelId);
        }
        Hotel hotel = hotels.get(hotelId);
        if (hotel == null) {
            throw new Exception("Hotel nicht gefunden: " + hotelId);
        }
        Logger.info("HotelBookingService", "Bestätigung erfolgreich für " + numberOfRooms + " Zimmer im Hotel " + hotel.getName() + ". Verbleibende Zimmer: " + hotel.getAvailableRooms());
        return "Bestätigung erfolgreich für " + numberOfRooms + " Zimmer im Hotel " + hotel.getName();
    }

    public synchronized void cancelBooking(String hotelId, int numberOfRooms) {
        Hotel hotel = hotels.get(hotelId);
        if (hotel != null) {
            hotel.cancelBooking(numberOfRooms);
            Logger.info("HotelBookingService", "Stornierung erfolgreich für " + numberOfRooms + " Zimmer im Hotel " + hotel.getName() + ". Verbleibende Zimmer: " + hotel.getAvailableRooms());
        } else {
            Logger.error("HotelBookingService", "Hotel nicht gefunden: " + hotelId);
        }
    }
}
