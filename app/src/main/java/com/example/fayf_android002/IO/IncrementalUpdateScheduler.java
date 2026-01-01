package com.example.fayf_android002.IO;


import com.example.fayf_android002.Config;
import com.example.fayf_android002.Entry.Entries;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class IncrementalUpdateScheduler {

    static Logger logger = Logger.getLogger(IncrementalUpdateScheduler.class.getName());

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean isRunning = false;

    public static void startPeriodicUpdates() {
        // prevent multiple schedulers
        if (isRunning) {
            logger.info("Incremental update scheduler is already running.");
            return;
        } else if (Config.TENANT.getValue().endsWith(Config.TENANT_TEST_SUFFIX)) {
            logger.info("Skipping incremental update scheduler for test tenant '" + Config.TENANT.getValue() + "'.");
            return;
        } else if (Config.AUTO_SYNC_YN.getBooleanValue()){
            logger.info("Starting incremental update scheduler as AUTO_SYNC_YN is enabled.");
        } else {
            logger.info("AUTO_SYNC_YN is disabled. Incremental update scheduler will not start.");
            return;
        }
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                Entries.loadDelta();
            } catch (Exception e) {
                logger.info("Error during incremental update: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.MINUTES); // Adjust the interval as needed
        isRunning = true;
    }

    public static void stopPeriodicUpdates() {
        scheduler.shutdown();
    }
}