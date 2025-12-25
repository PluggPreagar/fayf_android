package com.example.fayf_android002.Storage;

import android.content.Context;
import com.example.fayf_android002.Config;
import com.example.fayf_android002.Entry.*;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import com.example.fayf_android002.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.example.fayf_android002.Config.TENANT;

public class DataStorageLocal {

    private static final Logger logger = LoggerFactory.getLogger(DataStorageLocal.class);
    private final static String filePath = "entries_TID.dat.gz";
    private final static String filePathLocal = "config.dat.gz";

    private static Map<String, Long> lastModifiedMap = new HashMap<>();

    private static boolean isAlreadyProcessing(String fileName) {
        Long lastModified = lastModifiedMap.get(fileName);
        long currentTime = System.currentTimeMillis();
        if (lastModified != null && (currentTime - lastModified) < 1000) {
            logger.warn("Skipping load of {} as it was loaded less than 1 second ago.", fileName);
            return true;
        }
        lastModifiedMap.put(fileName, currentTime);
        return false;
    }

    public static void saveTenant(EntryTree entries, Context context)  {
        EntryTree dataEntries = EntryTree.getDataEntriesOnly(Entries.entryTree);
        save(dataEntries, filePath, context);
    }

    public static EntryTree loadTenant(Context context)  {
        return EntryTree.getDataEntriesOnly( load( filePath, context));
    }

    public static void saveLocal(Context context)  {
        EntryTree configEntries = EntryTree.getConfigEntriesOnly(Entries.entryTree);
        save(configEntries, filePathLocal, context);
    }

    public static EntryTree loadLocal(Context context)  {
        return EntryTree.getConfigEntriesOnly( load(filePathLocal, context));
    }

    // Serialize the EntryTree to a file
    public static void save(EntryTree entries, String filePathTmpl, Context context)  {
        UtilDebug.logCompactCallStack("DataStorageLocal.save to " + filePathTmpl);
        // inject tenantId into file name
        String type = filePathTmpl.contains("config") ? "config" : "entries";
        String filePath = filePathTmpl.replace("TID", Util.sanitizeFileNameWarn( Config.TENANT.getValue()));
        if (entries == null || entries.isEmpty()) {
            logger.warn("No {} to save to file: {}", type, filePath);
            return;
        }
        if (filePath.isEmpty()) {
            logger.error("Invalid file path for saving {}: {}", type, filePath);
            return;
        }
        if (!new File(context.getFilesDir(), filePath).exists()) {
            logger.warn("File ({}) does not exist, it will be created: {}", type, filePath);
        }
        if (isAlreadyProcessing(filePath)) {
            return;
        }
        logger.info("Saving {} {} to file: {} ...", entries.size(), type, filePath);
        logger.debug(" " + (new File(context.getFilesDir(), filePath).getAbsolutePath()));
        Entries.logEntries(entries, "Save " + type + " (" + filePath + ")");
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new GZIPOutputStream( context.openFileOutput(filePath, Context.MODE_PRIVATE)))) {
            oos.writeObject(entries.entries);
            logger.info("Saved to file: {} ({} {})", filePath, entries.size(), type);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error saving {} to file: {}", filePath, type, e);
        }
    }


    // Deserialize the EntryTree from a file
    public static EntryTree load(String filePathTmpl,  Context context)  {
        UtilDebug.logCompactCallStack("DataStorageLocal.load from " + filePathTmpl);
        String type = filePathTmpl.contains("config") ? "config" : "entries";
        String filePath = filePathTmpl.replace("TID", Util.sanitizeFileNameWarn( TENANT.getValue()));
        if (null == context) {
            logger.error("Context is null, cannot load entries from file: {}", filePath);
            return new EntryTree();
        }
        if (!new File(context.getFilesDir(), filePath).exists()) {
            logger.warn("Entries file does not exist: {}", filePath);
            return new EntryTree();
        }
        if (isAlreadyProcessing(filePath)) {
            return new EntryTree();
        }
        logger.info("Reading {} from file: {} ...", type, filePath);
        logger.debug(" " + (new File(context.getFilesDir(), filePath).getAbsolutePath()));
        EntryTree entries = new EntryTree();
        try (ObjectInputStream ois = new ObjectInputStream(
                new GZIPInputStream( context.openFileInput( filePath)))) {
            entries.entries = (SortedEntryTreeMap) ois.readObject();
            int sizeBefore = entries.size();
            logger.info("Read from file: {} ({} {})", filePath, entries.size(), type);
            Entries.logEntries(entries, "Loaded " + type + " (" + filePath + ")");
        } catch (Exception e) {
            logger.error("Error loading {} to file: {}", type, filePath, e);
            UtilDebug.logError("Error reading " + type + " from file: " + filePath , e);
        }
        return entries;
    }


}