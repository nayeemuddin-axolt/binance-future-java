package examples.Indicator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scheduler {

    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    public static void main(String[] args) {
        try {

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

            Runnable task = new Runnable() {
                public void run() {

                    logger.info("\n------- Process Start -------");

                    TradingStrategies.main(args);

                    logger.info("\n------- Process End -------");

                }
            };

            scheduler.scheduleAtFixedRate(task, 0, 5, TimeUnit.MINUTES);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}