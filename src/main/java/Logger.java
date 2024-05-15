import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    private static String getTimeStamp() {
        return dateFormat.format(new Date());
    }

    public static void info(String message) {
        System.out.println("INFO [" + getTimeStamp() + "][" + Thread.currentThread().getId() + "]: " + message);
    }

    public static void error(String message) {
        System.err.println("ERROR [" + getTimeStamp() + "][" + Thread.currentThread().getId() + "]: " + message);
    }

    public static void debug(String message) {
        System.out.println("DEBUG [" + getTimeStamp() + "][" + Thread.currentThread().getId() + "]: " + message);
    }
}
