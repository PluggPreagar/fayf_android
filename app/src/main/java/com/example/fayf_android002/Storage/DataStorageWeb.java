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

    public EntryTree readData(String sid, String tid) {
        EntryTree data = new EntryTree();
        readData(data, "https://fayf.info/entries?sid=" + sid + "&tid=" + tid);
        readData(data, "https://fayf.info/votes?sid=" + sid + "&tid=" + tid);
        return data;
    }

    // Fetch CSV data from a URL
    public EntryTree readData(EntryTree data, String urlString) {
        logger.info("Fetching data from URL: {}", urlString);

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    StringBuilder line_leftover = new StringBuilder();
                    int quotes = 0 ;
                    while ((line = reader.readLine()) != null) {
                        // count " in line - if odd, then continue reading
                        quotes += line.contains("\"")
                                ? line.length() - line.replace("\"", "").length()
                                : 0 ;
                        quotes = quotes % 2 ;
                        if (0 == quotes){ // even number of quotes - complete line
                            if (line_leftover.length() > 0) {
                                line = line_leftover + line ;
                                line_leftover.setLength(0);
                            }
                            add(data, line);
                        } else {
                            line_leftover.append(line).append("\n");
                        }
                    }
                }
            } else {
                logger.error("Failed to fetch data. {} \n Url: {}"
                        , responseCode
                        , urlString
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error fetching data from URL: {}", urlString, e);
        }

        return data;
    }


    private void add(EntryTree data, String line) {
        // entry:   2025-10-31 14:09:49,/ | 1761916190 | heute
        // entry:   2025-10-31 14:09:49,"/ | 1761916190 | heute, gestern oder morgen"
        // votes:   03/08/2025 10:52:30, | mig::Vote::97979 | Migration > | 1
        String[] rawParts = line.split(",", 2);
        String[] parts = rawParts.length > 1 ? rawParts[1].split(" \\| ", 3) : new String[]{};
        if (!rawParts[0].isEmpty() && parts.length > 2 && parts.length < 5 ) {
            // valid entry
            EntryKey key = new EntryKey(parts[0].trim(), parts[1].trim());
            // remove enclosing quotes if any
            if (parts[2].startsWith("\"") && parts[2].endsWith("\"")) {
                parts[2] = parts[2].substring(1, parts[2].length() - 1);
            }
            String content = parts[2].trim();
            if (key.topic.startsWith(Config.CONFIG_PATH)) {
                logger.debug("Skipping config entry from data load: {}", line);
            } else if (content.endsWith(DELETE_SUFFIX)) { // Suffix to allow safe APPEND operation
                data.remove(key);
            } else {
                data.load(key, content);
            }
        } else {
            logger.warn("Failed to build Entry from string: {}", line);
        }
    }

    public void saveEntry(EntryKey entryKey, Entry entry) {
        new Thread(() ->
                saveEntry(entryKey, entry, Config.SYSTEM.getValue(), Config.TENANT.getValue())
        ).start();
    }

    public void saveEntry(EntryKey entryKey, Entry entry, String sid, String tid) {
        assert sid != null ;
        assert tid != null ;
        assert !sid.isEmpty();
        String objectName = entryKey.nodeId.contains(EntryKey.VOTE_SEPARATOR) ? "vote" : "entry" ;
        // TODO do not use params ... in URL - use POST body !!
        String urlString = "https://fayf.info/" + objectName + "/add?" +
                "sid=" + sid + "&tid=" + tid +
                "&entry=" +  Util.encodeToUrlParam( buildStringRepresentation(entryKey, entry) );

        logger.info("Saving entry to URL: {}", urlString);
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

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
        if (entryKey.nodeId.contains(EntryKey.VOTE_SEPARATOR)) {
            // String voterId = Config.SYSTEM.getValue(); // TODO PERFORMANCE - cache system id !!
            int votes = entryKey.nodeId.endsWith(EntryKey.VOTE_SEPARATOR) ? entry.otherVotes : entry.myVote ;
            return entryKey.topic + " | " + entryKey.nodeId + " | " + entry.content + " | " + votes;
        }
        return entryKey.topic + " | " + entryKey.nodeId + " | " + entry.content;
    }


}