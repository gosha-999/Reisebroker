public class Hotel {
    private String id;
    private String name;
    private int availableRooms;

    public Hotel(String id, String name, int availableRooms) {
        this.id = id;
        this.name = name;
        this.availableRooms = availableRooms;
    }

    public synchronized boolean bookRooms(int number) {
        if (availableRooms >= number) {
            availableRooms -= number;
            return true;
        }
        return false;
    }

    public synchronized void cancelBooking(int number) {
        availableRooms += number;  // Zimmeranzahl wieder erhöhen
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
