package com.example.fayf_android002;

import android.content.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DataStorageLocal {

    private static final Logger logger = LoggerFactory.getLogger(DataStorageLocal.class);
    private final static String filePath = "entries.dat.gz";

    // Serialize the EntryTree to a file
    public static void saveEntries(TreeMap<String, TreeMap<String, Entry>> entries, Context context)  {
        if (entries == null || entries.isEmpty()) {
            logger.warn("No entries to save to file: {}", filePath);
            return;
        }
        // check if filePath is valid
        if (filePath == null || filePath.isEmpty()) {
            logger.error("Invalid file path for saving entries: {}", filePath);
            return;
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new GZIPOutputStream( context.openFileOutput(filePath, Context.MODE_PRIVATE)))) {
            oos.writeObject(entries);
            logger.info("Entries saved to file: {} ({} entries)", filePath, EntryTree.size(entries));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error saving entries to file: {}", filePath, e);
        }
    }


    // Deserialize the EntryTree from a file
    public static TreeMap<String, TreeMap<String, Entry>> loadEntries( Context context)  {
        TreeMap<String, TreeMap<String, Entry>> entries = new TreeMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(
                new GZIPInputStream( context.openFileInput( filePath)))) {
            entries = (TreeMap<String, TreeMap<String, Entry>>) ois.readObject();
            logger.info("Entries loaded from file: {} ({} entries)", filePath, EntryTree.size(entries));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error saving entries to file: {}", filePath, e);
        }
        return entries;
    }

}