package com.example.fayf_android002.Storage;

import android.content.Context;
import com.example.fayf_android002.Config;
import com.example.fayf_android002.Entry.Entries;
import com.example.fayf_android002.Entry.EntryKey;
import com.example.fayf_android002.Entry.EntryTree;
import com.example.fayf_android002.Entry.SortedEntryTreeMap;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.example.fayf_android002.Config.CONFIG_PATH;
import static com.example.fayf_android002.Config.TENANT;

public class DataStorageLocal {

    private static final Logger logger = LoggerFactory.getLogger(DataStorageLocal.class);
    private final static String filePath = "entries_TID.dat.gz";
    private final static String filePathGlobal = "config.dat.gz";

    // Serialize the EntryTree to a file
    public static void saveTenant(EntryTree entries, Context context)  {
        // inject tenantId into file name
        String filePath = DataStorageLocal.filePath.replace("TID", Config.TENANT.getValue());
        if (entries == null || entries.isEmpty()) {
            logger.warn("No entries to save to file: {}", filePath);
            return;
        }
        // check if filePath is valid
        if (filePath.isEmpty()) {
            logger.error("Invalid file path for saving entries: {}", filePath);
            return;
        }
        if (!new File(context.getFilesDir(), filePath).exists()) {
            logger.warn("Entries file does not exist, it will be created: {}", filePath);
        }
        logger.info("Saving {} entries to file: {} ...", entries.size(), filePath);
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new GZIPOutputStream( context.openFileOutput(filePath, Context.MODE_PRIVATE)))) {
            oos.writeObject(entries.entries);
            logger.info("Entries saved to file: {} ({} entries)", filePath, entries.size());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error saving entries to file: {}", filePath, e);
        }
    }


    // Deserialize the EntryTree from a file
    public static EntryTree loadTenant(Context context)  {
        String filePath = DataStorageLocal.filePath.replace("TID", TENANT.getValue());
        if (!new File(context.getFilesDir(), filePath).exists()) {
            logger.warn("Entries file does not exist: {}", filePath);
            return new EntryTree();
        }
        logger.info("Reading entries from file: {} ...", filePath);
        EntryTree entries = new EntryTree();
        try (ObjectInputStream ois = new ObjectInputStream(
                new GZIPInputStream( context.openFileInput( filePath)))) {
            entries.entries = (SortedEntryTreeMap) ois.readObject();
            int sizeBefore = entries.size();
            logger.info("Entries read from file: {} ({} entries)", filePath, entries.size());
            // remove all entries with hidden-prefix /_/ (including config)
            entries.entries.keySet().removeIf(topic -> topic.startsWith(CONFIG_PATH));
            entries.entries.keySet().removeIf(topic -> topic.startsWith("/_/"));
            int sizeAfter = entries.size();
            if (sizeBefore != sizeAfter) {
                logger.info("Removed {} hidden entries with prefix /_/ from loaded entries", (sizeBefore - sizeAfter));
            }
        } catch (Exception e) {
            UtilDebug.logError("Error reading entries from file: " + filePath , e);
            logger.error("Error loading entries to file: {}", filePath, e);
        }
        return entries;
    }


    // Save the configuration to a file
    public static void saveGlobal(Context context) {
        String filePath = filePathGlobal;
        if (!new File(context.getFilesDir(), filePath).exists()) {
            logger.warn("Config file does not exist: {}", filePath);
            return;
        }
        logger.info("Saving global to file: {} ...", filePath);
        Map<String, String> config =new HashMap<>();
        for (Config c : Config.values()) {
            config.put(c.getKey(), c.getValue());
        }
        // add other global values
        Entries.entryTree.entries.forEach((key, entryMap) -> {
            if (!key.equals(Config.CONFIG_PATH) && key.startsWith("/_/")) {
                logger.debug("Saving hidden global entries for topic: {}", key);
                entryMap.forEach((nodeId, entry) -> {
                    config.put(key + "/" + nodeId, entry.getContent());
                });
            }
        });
        //
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new GZIPOutputStream(context.openFileOutput(filePath, Context.MODE_PRIVATE)))) {
            oos.writeObject(config);
            logger.info("Global saved to file: {}", filePath);
        } catch (Exception e) {
            logger.error("Error saving global to file: {}", filePath, e);
        }
    }

    // Load the configuration from a file
    public static void loadGlobal(Context context) {
        String filePath = filePathGlobal;
        if (!new File(context.getFilesDir(), filePath).exists()) {
            logger.warn("Global file does not exist: {}", filePath);
            return;
        }
        logger.info("Reading global from file: {} ...", filePath);
        try (ObjectInputStream ois = new ObjectInputStream(
                new GZIPInputStream(context.openFileInput(filePath)))) {
            Map<String, String> config = (Map<String, String>) ois.readObject();
            for (Map.Entry<String, String> e : config.entrySet()) {
                if (e.getKey().contains("/")) {
                    // hidden "/_/" entries
                    // allow loading config additional hidden entries like /_/config/tenant/[tst,tst5 ... ]
                    EntryKey entryKey = new EntryKey(e.getKey());
                    if (Config.CONFIG_PATH.equals(entryKey.topic)) {
                        logger.debug("Skipping config-dupl: {} (set by config already)", e.getKey());
                    } else {
                        Entries.setEntryInternal(entryKey.topic, entryKey.nodeId, e.getValue());
                        logger.debug("Global hidden entry loaded: {} -> {}", entryKey, e.getValue());
                    }
                } else {
                    Config.set(e.getKey(), e.getValue());
                }
            }
            logger.info("Global read from file: {}", filePath);
        } catch (Exception e) {
            logger.error("Error loading global from file: {}", filePath, e);
        }
        // hidden "/_/" entries
        Entries.setEntryInternal("/_","ToDo", "ToDo"); // ensure config path exists
    }

}