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

        service1.addHotel(new Hotel("H001", "Hotel California", 50));
        service1.addHotel(new Hotel("H002", "Hotel Nevada", 30));
        service1.addHotel(new Hotel("H003", "Hotel Texas", 20));
        service1.addHotel(new Hotel("H004", "Hotel Arizona", 40));
        service1.addHotel(new Hotel("H005", "Hotel Colorado", 35));

        service2.addHotel(new Hotel("H006", "Hotel Florida", 40));
        service2.addHotel(new Hotel("H007", "Hotel New York", 25));
        service2.addHotel(new Hotel("H008", "Hotel Chicago", 35));
        service2.addHotel(new Hotel("H009", "Hotel Michigan", 30));
        service2.addHotel(new Hotel("H010", "Hotel Ohio", 20));

        // Starten einer Endlosschleife zur kontinuierlichen Generierung von Buchungsanfragen
        while (true) {
            List<Thread> threads = new ArrayList<>();
            threads.add(new Thread(() -> sendTripRequests("Reise 1", createTrip(service1, new String[]{"H001", "H002", "H003", "H004", "H005"}))));
            threads.add(new Thread(() -> sendTripRequests("Reise 2", createTrip(service2, new String[]{"H006", "H007", "H008", "H009", "H010"}))));

            // Starten der Threads mit zufälligen Verzögerungen
            for (Thread thread : threads) {
                thread.start();
                try {
                    int delay = MIN_DELAY + new Random().nextInt(MAX_DELAY - MIN_DELAY); // Zufällige Verzögerung zwischen den Starts der Reisen
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Verzögerung zwischen den Startzyklen der Reisen
            try {
                Thread.sleep(10000); // 10 Sekunden Verzögerung vor dem nächsten Zyklus
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
    }
}
