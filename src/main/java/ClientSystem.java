import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClientSystem {
    private static final int MIN_DELAY = 3000; // Minimale Verzögerung in Millisekunden (3 Sekunden)
    private static final int MAX_DELAY = 6000; // Maximale Verzögerung in Millisekunden (6 Sekunden)

    public static void main(String[] args) {
        // Erstellen und Hinzufügen von HotelBookingServices und Hotels
        HotelBookingService service1 = new HotelBookingService();
        HotelBookingService service2 = new HotelBookingService();
        HotelBookingService service3 = new HotelBookingService();

        service1.addHotel(new Hotel("H001", "Hotel California", 50));
        service1.addHotel(new Hotel("H002", "Hotel Nevada", 30));
        service1.addHotel(new Hotel("H003", "Hotel Texas", 20));

        service2.addHotel(new Hotel("H004", "Hotel Florida", 40));
        service2.addHotel(new Hotel("H005", "Hotel New York", 25));
        service2.addHotel(new Hotel("H006", "Hotel Chicago", 35));

        service3.addHotel(new Hotel("H007", "Hotel Seattle", 45));
        service3.addHotel(new Hotel("H008", "Hotel Denver", 50));
        service3.addHotel(new Hotel("H009", "Hotel Boston", 30));

        // Erstellen von verschiedenen Reisen
        List<BookingRequest> trip1 = createTrip(service1, new String[]{"H001", "H002", "H003"});
        List<BookingRequest> trip2 = createTrip(service2, new String[]{"H004", "H005", "H006"});
        List<BookingRequest> trip3 = createTrip(service3, new String[]{"H007", "H008", "H009"});

        // Senden der Buchungsanfragen
        new Thread(() -> sendTripRequests("Reise 1", trip1)).start();
        new Thread(() -> sendTripRequests("Reise 2", trip2)).start();
        new Thread(() -> sendTripRequests("Reise 3", trip3)).start();
    }

    private static List<BookingRequest> createTrip(HotelBookingService service, String[] hotelIds) {
        Random random = new Random();
        List<BookingRequest> requests = new ArrayList<>();
        for (String hotelId : hotelIds) {
            int rooms = random.nextInt(5) + 1; // Zufällige Anzahl von Zimmern (1 bis 5)
            requests.add(new BookingRequest(service, hotelId, rooms));
        }
        return requests;
    }

    private static void sendTripRequests(String tripName, List<BookingRequest> requests) {
        TravelBroker travelBroker = TravelBroker.getInstance();
        Logger.info("Starte Anfragen für " + tripName);
        travelBroker.bookTravel(requests);
        try {
            Random random = new Random();
            int delay = MIN_DELAY + random.nextInt(MAX_DELAY - MIN_DELAY); // Zufällige Verzögerung zwischen den Starts der Reisen
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
