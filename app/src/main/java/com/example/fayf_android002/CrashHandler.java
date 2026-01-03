package com.example.fayf_android002;

import android.content.Context;
import android.os.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CrashHandler.class);
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final Context context;
    private final String crashFile = "crash_log.txt";
    String serverUrl = "https://fayf.info/dump?" +
            "sid=" + Config.SYSTEM.getValue() +
            "&tenant=" + Config.TENANT.getValue() +
            "&version=" + Config.VERSION.getValue() +
            "&platform=android";

    public CrashHandler(Context context) {
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.context = context;
        uploadCrashData( new File(crashFile));
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            // Log crash details to a file
            File crashFile = new File(context.getFilesDir(), this.crashFile);
            try (FileWriter writer = new FileWriter(crashFile, true);
                 PrintWriter printWriter = new PrintWriter(writer)) {
                printWriter.println("Crash Timestamp: " + System.currentTimeMillis());
                printWriter.println("Thread: " + thread.getName());
                printWriter.println("Device: " + Build.MANUFACTURER + " " + Build.MODEL);
                printWriter.println("Android Version: " + Build.VERSION.RELEASE);
                printWriter.println("-------------------------");
                for (Config config : Config.values()) {
                    printWriter.println("Config " + config + ": " + config.getValue());
                };
                throwable.printStackTrace(printWriter);
            }
            logger.error("Crash logged to file: {}", crashFile.getAbsolutePath());

            // Upload crash data to server
            uploadCrashData(crashFile);
        } catch (Exception e) {
            logger.error("Error while handling crash", e);
        } finally {
            // Pass the exception to the default handler
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        }
    }

    private void uploadCrashData(File crashFile) {
        if (!crashFile.exists()) {
            logger.info("No crash file to upload");
            return;
        }
        new Thread(() -> {
            try {
                // Example: Upload crash file to server
                HttpURLConnection connection = (HttpURLConnection) new URL(serverUrl).openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                try (FileInputStream fis = new FileInputStream(crashFile);
                     OutputStream os = connection.getOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    logger.info("Crash data uploaded successfully");
                    // crashfile are archived and deleted
                    File archivedFile = new File(crashFile.getAbsolutePath() + ".uploaded");
                    if (archivedFile.exists()) {
                        archivedFile.delete();
                    }
                    crashFile.renameTo(archivedFile);
                } else {
                    logger.error("Failed to upload crash data. Response code: {}", responseCode);
                }
            } catch (Exception e) {
                logger.error("Error uploading crash data", e);
            }
        }).start();
    }
}