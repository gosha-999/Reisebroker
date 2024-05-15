public class MessageBroker {
    private static MessageBroker instance = new MessageBroker();

    private MessageBroker() {}

    public static MessageBroker getInstance() {
        return instance;
    }

    public void sendMessage(HotelBookingService service, String hotelId, int numberOfRooms, TravelBroker travelBroker) {
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulierte Latenz
                String response = service.bookHotelRooms(hotelId, numberOfRooms);
                travelBroker.receiveMessage(response);
            } catch (Exception e) {
                travelBroker.receiveMessage("Fehler: " + e.getMessage());
            }
        }).start();
    }
}
