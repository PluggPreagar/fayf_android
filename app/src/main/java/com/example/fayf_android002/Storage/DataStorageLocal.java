package com.example.fayf_android002.Storage;

import android.content.Context;
import com.example.fayf_android002.Config;
import com.example.fayf_android002.Entry.Entry;
import com.example.fayf_android002.Entry.EntryTree;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.example.fayf_android002.Config.TENANT;

public class DataStorageLocal {

    private static final Logger logger = LoggerFactory.getLogger(DataStorageLocal.class);
    private final static String filePath = "entries_TID.dat.gz";

    // Serialize the EntryTree to a file
    public static void saveEntries(EntryTree entries, Context context)  {
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
    public static EntryTree loadEntries(Context context)  {
        String filePath = DataStorageLocal.filePath.replace("TID", TENANT.getValue());
        logger.info("Reading entries from file: {} ...", filePath);
        EntryTree entries = new EntryTree();
        try (ObjectInputStream ois = new ObjectInputStream(
                new GZIPInputStream( context.openFileInput( filePath)))) {
            entries.entries = (TreeMap<String, TreeMap<String, Entry>>) ois.readObject();
            logger.info("Entries read from file: {} ({} entries)", filePath, entries.size());
        } catch (Exception e) {
            UtilDebug.logError("Error reading entries from file: " + filePath , e);
            logger.error("Error loading entries to file: {}", filePath, e);
        }
        return entries;
    }

}