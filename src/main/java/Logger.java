import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    // ANSI escape codes for colors
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    private static String getTimeStamp() {
        return dateFormat.format(new Date());
    }

    public static void info(String component, String message) {
        String color = getColor(component);
        System.out.println(color + "INFO [" + getTimeStamp() + "][" + component + "]: " + message + RESET);
    }

    public static void error(String component, String message) {
        String color = getColor(component);
        System.err.println(color + "ERROR [" + getTimeStamp() + "][" + component + "]: " + message + RESET);
    }

    public static void debug(String component, String message) {
        String color = getColor(component);
        System.out.println(color + "DEBUG [" + getTimeStamp() + "][" + component + "]: " + message + RESET);
    }

    private static String getColor(String component) {
        switch (component) {
            case "ClientSystem":
                return GREEN;
            case "TravelBroker":
                return BLUE;
            case "MessageBroker":
                return PURPLE;
            case "HotelBookingService":
                return CYAN;
            case "Hotel":
                return YELLOW;
            default:
                return WHITE;
        }
    }
}
