public class Hotel {
    private String id;
    private String name;
    private int availableRooms;

    public Hotel(String id, String name, int availableRooms) {
        this.id = id;
        this.name = name;
        this.availableRooms = availableRooms;
    }

    public synchronized boolean bookRooms(int numberOfRooms) {
        if (availableRooms >= numberOfRooms) {
            availableRooms -= numberOfRooms;
            return true;
        }
        return false;
    }

    public synchronized void cancelBooking(int numberOfRooms) {
        availableRooms += numberOfRooms;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAvailableRooms() {
        return availableRooms;
    }
}
