public class MessageBroker {
    private static MessageBroker instance = new MessageBroker();

    private MessageBroker() {}

    public static MessageBroker getInstance() {
        return instance;
    }

    public void sendMessage(BookingRequest request, TravelBroker travelBroker) {
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulierte Latenz
                String response = request.getService().bookHotelRooms(request.getHotelId(), request.getNumberOfRooms());
                travelBroker.receiveMessage(response);
            } catch (Exception e) {
                travelBroker.receiveMessage("Fehler: " + e.getMessage());
            }
        }).start();
    }
}
