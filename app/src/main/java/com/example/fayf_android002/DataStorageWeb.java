package com.example.fayf_android002;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DataStorageWeb {

    public static String SID_DEFAULT = "sid_example";
    public static String TID_DEFAULT = "tst5";

    private final static Logger logger = LoggerFactory.getLogger(DataStorageWeb.class);

    public List<String> readData() {
        return readData(SID_DEFAULT, TID_DEFAULT);
    }

    // Fetch CSV data from a URL
    public List<String> readData(String sid, String tid) {
        List<String> data = new ArrayList<>();

        String urlString = "https://fayf.info/entry/get?sid=" + sid + "&tid=" + tid;
        logger.info("Fetching data from URL: {}", urlString);

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        data.add(line);
                    }
                }
            } else {
                logger.error("Failed to fetch data. HTTP response code: {}", responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error fetching data from URL: {}", urlString, e);
        }

        return data;
    }

    public void saveEntry(Entry entry) {
        saveEntry(entry, SID_DEFAULT, TID_DEFAULT);
    }
    public void saveEntry(Entry entry, String sid, String tid) {

        String urlString = "https://fayf.info/entry/add?sid=" + sid + "&tid=" + tid +
                "&entry=" +  Util.encodeToUrlParam( entry.buildStringRepresentation() );

        logger.info("Saving entry to URL: {}", urlString);
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("Entry saved successfully: {}", entry.buildStringRepresentation());
            } else {
                logger.error("Failed to save entry. HTTP response code: {}", responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error saving entry to URL: {}", urlString, e);
        }
    }

}