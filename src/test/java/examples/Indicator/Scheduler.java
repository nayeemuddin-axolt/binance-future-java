package examples.Indicator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.*;
import java.util.logging.*;

public class Scheduler {
    public static void main(String[] args) {
        try {
            Logger logger = Logger.getLogger(TradingStrategies.class.getName());

            String logDir = "src/test/java/examples/Logs/";
            String logFileName = getLogFileName();

            // Create a file handler with the log file path and filename
            FileHandler fileHandler = new FileHandler(logDir + logFileName, true);

            // Create a formatter for log records
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

            // Add the file handler to the logger
            logger.addHandler(fileHandler);

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

            Runnable task = new Runnable() {
                public void run() {

                    logger.info("------- Time Start at " + LocalTime.now() + " -------");

                    TradingStrategies.main(args);

                    logger.info("------- Time End at " + LocalTime.now() + " -------");

                }
            };

            scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.MINUTES);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getLogFileName() {
        // Get the current date
        java.util.Date date = new java.util.Date();

        // Format the date as "yyyyMMdd"
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(date);

        // Return the log filename with the formatted date
        return "log_" + formattedDate + ".txt";
    }
}