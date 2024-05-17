public class MessageBroker {
    private static MessageBroker instance = new MessageBroker();

    private MessageBroker() {}

    public static MessageBroker getInstance() {
        return instance;
    }

    public void sendMessage(BookingRequest request, BookingContext context, int attempt, boolean isConfirmation) {
        Logger.info("MessageBroker", "Sende Buchungsanfrage für Hotel " + request.getHotelId() + ", Zimmer: " + request.getNumberOfRooms() + ". Versuch: " + (attempt + 1));
        request.getService().processBookingRequest(request, attempt, isConfirmation);
    }

    public void sendResponse(String message, BookingRequest request, TravelBroker travelBroker, int attempt, BookingContext context, boolean isConfirmation) {
        Logger.info("MessageBroker", "Antwort erhalten für Hotel " + request.getHotelId() + ": " + message);
        travelBroker.receiveMessage(message, request, attempt, context, isConfirmation);
    }
}
