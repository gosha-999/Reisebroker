import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static Properties properties = new Properties();

    //lädt die Variablen aus der config.properties Datei:
    static {
        try {
            properties.load(new FileInputStream("src/main/java/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //Wird verwendet, um den Wert einer nach dem key benannten Variablen aus der Config zu erhalten
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
