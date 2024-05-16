import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class
ClientSystem {
    private static final int ARRIVAL_RATE = Integer.parseInt(Config.getProperty("arrivalRate"));
    private static AtomicInteger tripCounter = new AtomicInteger(1); // Zähler für die Reisen

    public static void main(String[] args) {
        // Erstellen und Hinzufügen von HotelBookingServices und Hotels
        HotelBookingService service1 = new HotelBookingService();
        HotelBookingService service2 = new HotelBookingService();

        service1.addHotel(new Hotel("H001", "Hotel 1", 50));
        service1.addHotel(new Hotel("H002", "Hotel 2", 30));
        service1.addHotel(new Hotel("H003", "Hotel 3", 20));
        service1.addHotel(new Hotel("H004", "Hotel 4", 40));
        service1.addHotel(new Hotel("H005", "Hotel 5", 35));

        service2.addHotel(new Hotel("H006", "Hotel 6", 40));
        service2.addHotel(new Hotel("H007", "Hotel 7", 25));
        service2.addHotel(new Hotel("H008", "Hotel 8", 35));
        service2.addHotel(new Hotel("H009", "Hotel 9", 30));
        service2.addHotel(new Hotel("H010", "Hotel 10", 20));

        // Liste aller Hotels mit den zugehörigen Services
        List<Hotel> allHotels = new ArrayList<>();
        allHotels.addAll(service1.getHotels());
        allHotels.addAll(service2.getHotels());

        // Liste der Services
        List<HotelBookingService> services = new ArrayList<>();
        services.add(service1);
        services.add(service2);

        // Starten einer Endlosschleife zur kontinuierlichen Generierung von Buchungsanfragen
        while (true) {
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < 4; i++) { // Vier gleichzeitige Reisen
                threads.add(new Thread(() -> sendTripRequests(createTrip(services, allHotels))));
            }

            // Starten der Threads mit zufälligen Verzögerungen
            for (Thread thread : threads) {
                thread.start();
                try {
                    int delay = ARRIVAL_RATE + new Random().nextInt(ARRIVAL_RATE); // Zufällige Verzögerung zwischen den Starts der Reisen
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static List<BookingRequest> createTrip(List<HotelBookingService> services, List<Hotel> allHotels) {
        Random random = new Random();
        List<BookingRequest> requests = new ArrayList<>();
        for (int i = 0; i < 5; i++) { // Fünf verschiedene Hotels pro Reise
            Hotel hotel = allHotels.get(random.nextInt(allHotels.size()));
            int rooms = random.nextInt(5) + 1; // Zufällige Anzahl von Zimmern (1 bis 5)
            HotelBookingService service = services.stream().filter(s -> s.hasHotel(hotel)).findFirst().orElse(null);
            requests.add(new BookingRequest(service, hotel.getId(), rooms));
        }
        return requests;
    }

    private static void sendTripRequests(List<BookingRequest> requests) {
        TravelBroker travelBroker = TravelBroker.getInstance();
        int tripNumber = tripCounter.getAndIncrement(); // Fortlaufende Nummer für die Reise
        String tripName = "Reise " + tripNumber;
        Logger.hotelRequest("ClientSystem", "Starte Anfragen für " + tripName);
        travelBroker.bookTravel(requests);
    }
}
