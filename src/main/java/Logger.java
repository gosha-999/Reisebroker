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

    public static void info(String message) {
        System.out.println(GREEN + "INFO [" + getTimeStamp() + "][" + Thread.currentThread().getId() + "]: " + message + RESET);
    }

    public static void error(String message) {
        System.err.println(RED + "ERROR [" + getTimeStamp() + "][" + Thread.currentThread().getId() + "]: " + message + RESET);
    }

    public static void debug(String message) {
        System.out.println(BLUE + "DEBUG [" + getTimeStamp() + "][" + Thread.currentThread().getId() + "]: " + message + RESET);
    }
}
