public class MessageBroker {
    private static MessageBroker instance = new MessageBroker();
    private static final int MESSAGE_DELAY = Integer.parseInt(Config.getProperty("messageDelay"));

    private MessageBroker() {}

    public static MessageBroker getInstance() {
        return instance;
    }

    public void sendMessage(BookingRequest request, BookingContext context, int attempt, boolean isConfirmation) {
        Logger.info("MessageBroker", "Sende Buchungsanfrage für Hotel " + request.getHotelId() + ", Zimmer: " + request.getNumberOfRooms() + ". Versuch: " + (attempt + 1));
        try {
            Thread.sleep(MESSAGE_DELAY); // Simulierte Verzögerung
        } catch (InterruptedException e) {
            Logger.error("MessageBroker", "Verzögerung unterbrochen bei der Buchungsanfrage für Hotel " + request.getHotelId());
        }
        request.getService().processBookingRequest(request, attempt, isConfirmation);
    }

    public void sendResponse(String message, BookingRequest request, TravelBroker travelBroker, int attempt, BookingContext context, boolean isConfirmation) {
        Logger.info("MessageBroker", "Antwort erhalten für Hotel " + request.getHotelId() + ": " + message);
        try {
            Thread.sleep(MESSAGE_DELAY); // Simulierte Verzögerung
        } catch (InterruptedException e) {
            Logger.error("MessageBroker", "Verzögerung unterbrochen bei der Antwort für Hotel " + request.getHotelId());
        }
        travelBroker.receiveMessage(message, request, attempt, context, isConfirmation);
    }
}
