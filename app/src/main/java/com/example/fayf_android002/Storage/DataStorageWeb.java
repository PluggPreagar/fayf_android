package com.example.fayf_android002.Storage;

import com.example.fayf_android002.Config;
import com.example.fayf_android002.Entry.Entry;
import com.example.fayf_android002.Entry.EntryKey;
import com.example.fayf_android002.Entry.EntryTree;
import com.example.fayf_android002.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DataStorageWeb {

    private final static Logger logger = LoggerFactory.getLogger(DataStorageWeb.class);


    String DELETE_SUFFIX = "--";

    public EntryTree readData() {
        return readData(Config.SYSTEM.getValue(), Config.TENANT.getValue());
    }

    // Fetch CSV data from a URL
    public EntryTree readData(String sid, String tid) {
        EntryTree data = new EntryTree();

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
                        add(data, line);
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


    private void add(EntryTree data, String line) {
        // 2025-10-31 14:09:49,/ | 1761916190 | heute
        String[] rawParts = line.split(",", 2);
        String[] parts = rawParts.length > 1 ? rawParts[1].split(" \\| ", 3) : new String[]{};
        if (!rawParts[0].isEmpty() && parts.length > 2 && parts.length < 5 ) {
            // valid entry
            EntryKey key = new EntryKey(parts[0].trim(), parts[1].trim());
            String content = parts[2].trim();
            if (content.endsWith(DELETE_SUFFIX)) { // Suffix to allow safe APPEND operation
                data.remove(key);
            } else {
                data.set(key, content);
            }
        } else {
            logger.warn("Failed to build Entry from string: {}", line);
        }
    }

    public void saveEntry(EntryKey entryKey, Entry entry) {
        saveEntry(entryKey, entry, Config.SYSTEM.getValue(), Config.TENANT.getValue());
    }

    public void saveEntry(EntryKey entryKey, Entry entry, String sid, String tid) {

        String urlString = "https://fayf.info/entry/add?sid=" + sid + "&tid=" + tid +
                "&entry=" +  Util.encodeToUrlParam( buildStringRepresentation(entryKey, entry) );

        logger.info("Saving entry to URL: {}", urlString);
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("Entry saved successfully: {}", buildStringRepresentation(entryKey, entry));
            } else {
                logger.error("Failed to save entry. HTTP response code: {}", responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error saving entry to URL: {}", urlString, e);
        }
    }

    private String buildStringRepresentation(EntryKey entryKey, Entry entry) {
        return entryKey.topic + " | " + entryKey.nodeId + " | " + entry.content;
    }


}