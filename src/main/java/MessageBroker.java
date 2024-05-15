public class MessageBroker {
    private static MessageBroker instance = new MessageBroker();

    private MessageBroker() {}

    public static MessageBroker getInstance() {
        return instance;
    }

    public void sendMessage(BookingRequest request, TravelBroker travelBroker, int attempt) {
        new Thread(() -> {
            try {
                String response = request.getService().bookHotelRooms(request.getHotelId(), request.getNumberOfRooms());
                travelBroker.receiveMessage(response, request, attempt);
            } catch (Exception e) {
                travelBroker.receiveMessage("Fehler: " + e.getMessage(), request, attempt);
            }
        }).start();
    }
}
