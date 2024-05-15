public class BookingRequest {
    private HotelBookingService service;
    private String hotelId;
    private int numberOfRooms;

    public BookingRequest(HotelBookingService service, String hotelId, int numberOfRooms) {
        this.service = service;
        this.hotelId = hotelId;
        this.numberOfRooms = numberOfRooms;
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
}
