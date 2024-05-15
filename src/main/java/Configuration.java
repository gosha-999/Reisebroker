import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Configuration {
    public int arrivalRate;
    public int processingTime;
    public int messageDelay;
    public double technicalFailureProbability;
    public double businessFailureProbability;
    public int normalDistributionMean;
    public int normalDistributionStdDev;
    public int maxRetries;
    public int retryDelay;

    public static Configuration loadConfig(String filename) {
        Properties properties = new Properties();
        Configuration config = new Configuration();
        try {
            properties.load(Files.newBufferedReader(Paths.get(filename)));
            config.arrivalRate = Integer.parseInt(properties.getProperty("arrivalRate"));
            config.processingTime = Integer.parseInt(properties.getProperty("processingTime"));
            config.messageDelay = Integer.parseInt(properties.getProperty("messageDelay"));
            config.technicalFailureProbability = Double.parseDouble(properties.getProperty("technicalFailureProbability"));
            config.businessFailureProbability = Double.parseDouble(properties.getProperty("businessFailureProbability"));
            config.normalDistributionMean = Integer.parseInt(properties.getProperty("normalDistributionMean"));
            config.normalDistributionStdDev = Integer.parseInt(properties.getProperty("normalDistributionStdDev"));
            config.maxRetries = Integer.parseInt(properties.getProperty("maxRetries"));
            config.retryDelay = Integer.parseInt(properties.getProperty("retryDelay"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return config;
    }
}
