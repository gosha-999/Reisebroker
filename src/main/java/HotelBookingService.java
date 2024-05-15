import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HotelBookingService {
    private Map<String, Hotel> hotels;
    private ExecutorService executorService;
    private static final double TECHNICAL_FAILURE_PROBABILITY = 0.1; // Wahrscheinlichkeit eines technischen Fehlers
    private static final double BUSINESS_FAILURE_PROBABILITY = 0.2; // Wahrscheinlichkeit eines fachlichen Fehlers

    public HotelBookingService() {
        this.hotels = new HashMap<>();
        this.executorService = Executors.newCachedThreadPool(); // Thread-Pool zur parallelen Verarbeitung
    }

    public void addHotel(Hotel hotel) {
        hotels.put(hotel.getId(), hotel);
    }

    public synchronized String bookHotelRooms(String hotelId, int numberOfRooms) throws Exception {
        if (new Random().nextDouble() < TECHNICAL_FAILURE_PROBABILITY) {
            throw new Exception("Technischer Fehler bei der Buchung von Hotel " + hotelId);
        }
        Hotel hotel = hotels.get(hotelId);
        if (hotel != null && hotel.bookRooms(numberOfRooms)) {
            return "Buchung erfolgreich für " + numberOfRooms + " Zimmer im Hotel: " + hotel.getName();
        }
        if (new Random().nextDouble() < BUSINESS_FAILURE_PROBABILITY) {
            return "Fachlicher Fehler bei der Buchung von Hotel " + hotelId + ": Zimmer nicht verfügbar";
        }
        return "Buchung fehlgeschlagen für " + numberOfRooms + " Zimmer im Hotel: " + hotelId;
    }

    public synchronized void cancelBooking(String hotelId, int numberOfRooms) {
        Hotel hotel = hotels.get(hotelId);
        if (hotel != null) {
            hotel.cancelBooking(numberOfRooms);
            Logger.info("Stornierung erfolgreich für " + numberOfRooms + " Zimmer im Hotel: " + hotel.getName());
        } else {
            Logger.error("Hotel nicht gefunden: " + hotelId);
        }
    }
}
