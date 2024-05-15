public class MessageBroker {
    private static MessageBroker instance = new MessageBroker();
    private static final int MESSAGE_DELAY = Integer.parseInt(Config.getProperty("messageDelay"));

    private MessageBroker() {}

    public static MessageBroker getInstance() {
        return instance;
    }

    public void sendMessage(BookingRequest request, TravelBroker travelBroker, int attempt, boolean isConfirmation) {
        new Thread(() -> {
            try {
                Thread.sleep(MESSAGE_DELAY); // Simulierte Verzögerung der Nachrichtenzustellung
                Logger.debug("MessageBroker", "Weiterleitung der Anfrage an HotelBookingService für Hotel: " + request.getHotelId());
                request.getService().processBookingRequest(request, attempt, isConfirmation);
            } catch (Exception e) {
                travelBroker.receiveMessage("Fehler: " + e.getMessage(), request, attempt, isConfirmation);
            }
        }).start();
    }

    public void sendResponse(String message, BookingRequest request, TravelBroker travelBroker, int attempt, boolean isConfirmation) {
        new Thread(() -> {
            try {
                Thread.sleep(MESSAGE_DELAY); // Simulierte Verzögerung der Nachrichtenzustellung
                Logger.debug("MessageBroker", "Weiterleitung der Antwort an TravelBroker für Hotel: " + request.getHotelId());
                travelBroker.receiveMessage(message, request, attempt, isConfirmation);
            } catch (Exception e) {
                travelBroker.receiveMessage("Fehler: " + e.getMessage(), request, attempt, isConfirmation);
            }
        }).start();
    }
}
