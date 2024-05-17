public class BookingRequest {
    private HotelBookingService service;
    private String hotelId;
    private int numberOfRooms;
    private BookingContext context;

    public BookingRequest(HotelBookingService service, String hotelId, int numberOfRooms, BookingContext context) {
        this.service = service;
        this.hotelId = hotelId;
        this.numberOfRooms = numberOfRooms;
        this.context = context;
    }

    public HotelBookingService getService() {
        return service;
    }

    public String getHotelId() {
        return hotelId;
    }

    public int getNumberOfRooms() {
        return numberOfRooms;
    }

    public BookingContext getContext() {
        return context;
    }

    public void setContext(BookingContext context) {
        this.context = context;
    }
}
