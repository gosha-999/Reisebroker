import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HotelBookingService {
    private Map<String, Hotel> hotels;
    private ExecutorService executorService;
    private static final double TECHNICAL_FAILURE_PROBABILITY = Double.parseDouble(Config.getProperty("technicalFailureProbability"));
    private static final double BUSINESS_FAILURE_PROBABILITY = Double.parseDouble(Config.getProperty("businessFailureProbability"));
    private static final int PROCESSING_TIME = Integer.parseInt(Config.getProperty("processingTime"));

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
                Logger.debug("HotelBookingService", "Antwort an MessageBroker für Hotel: " + request.getHotelId());
                MessageBroker.getInstance().sendResponse(response, request, TravelBroker.getInstance(), attempt, isConfirmation);
            } catch (Exception e) {
                Logger.probelm("HotelBookingService", "Fehler beim Verarbeiten der Buchung für Hotel: " + request.getHotelId());
                MessageBroker.getInstance().sendResponse("Fehler: " + e.getMessage(), request, TravelBroker.getInstance(), attempt, isConfirmation);
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
                throw new Exception("Fachlicher Fehler: Buchungsbestätigung nicht übermittelt");
            }
            return "Buchung erfolgreich für " + numberOfRooms + " Zimmer im Hotel: " + hotel.getName();
        } else {
            return "Fachlicher Fehler bei der Buchung von Hotel " + hotelId + ": Zimmer nicht verfügbar";
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
        if (hotel.getAvailableRooms() < numberOfRooms) {
            throw new Exception("Fachlicher Fehler: Zimmer nicht verfügbar");
        }
        return "Bestätigung erfolgreich für " + numberOfRooms + " Zimmer im Hotel: " + hotel.getName();
    }

    public synchronized void cancelBooking(String hotelId, int numberOfRooms) {
        Hotel hotel = hotels.get(hotelId);
        if (hotel != null) {
            hotel.cancelBooking(numberOfRooms);
            Logger.info("HotelBookingService", "Stornierung erfolgreich für " + numberOfRooms + " Zimmer im Hotel: " + hotel.getName());
        } else {
            Logger.error("HotelBookingService", "Hotel nicht gefunden: " + hotelId);
        }
    }
}
