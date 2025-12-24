package com.example.fayf_android002.Storage;

import android.content.Context;
import com.example.fayf_android002.Config;
import com.example.fayf_android002.Entry.*;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import com.example.fayf_android002.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.example.fayf_android002.Config.CONFIG_PATH;
import static com.example.fayf_android002.Config.TENANT;

public class DataStorageLocal {

    private static final Logger logger = LoggerFactory.getLogger(DataStorageLocal.class);
    private final static String filePath = "entries_TID.dat.gz";
    private final static String filePathLocal = "config.dat.gz";



    // Serialize the EntryTree to a file
    public static void saveTenant(EntryTree entries, Context context)  {
        // inject tenantId into file name
        String filePath = DataStorageLocal.filePath.replace("TID", Util.sanitizeFileNameWarn( Config.TENANT.getValue()));
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
        logger.debug(" " + (new File(context.getFilesDir(), filePath).getAbsolutePath()));
        Entries.logEntries(entries, "Entries to save (" + filePath + ")");
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
        String filePath = DataStorageLocal.filePath.replace("TID", Util.sanitizeFileNameWarn( TENANT.getValue()));
        if (null == context) { // TODO do we need context?
            logger.error("Context is null, cannot load entries from file: {}", filePath);
            return new EntryTree();
        }
        if (!new File(context.getFilesDir(), filePath).exists()) {
            logger.warn("Entries file does not exist: {}", filePath);
            return new EntryTree();
        }
        logger.info("Reading entries from file: {} ...", filePath);
        logger.debug(" " + (new File(context.getFilesDir(), filePath).getAbsolutePath()));
        EntryTree entries = new EntryTree();
        try (ObjectInputStream ois = new ObjectInputStream(
                new GZIPInputStream( context.openFileInput( filePath)))) {
            entries.entries = (SortedEntryTreeMap) ois.readObject();
            int sizeBefore = entries.size();
            logger.info("Entries read from file: {} ({} entries)", filePath, entries.size());
            int sizeAfter = entries.size();
            if (sizeBefore != sizeAfter) {
                logger.info("Removed {} hidden entries with prefix /_/ from loaded entries", (sizeBefore - sizeAfter));
            }
            Entries.logEntries(entries, "Entries loaded (" + filePath + ")");
        } catch (Exception e) {
            logger.error("Error loading entries to file: {}", filePath, e);
            UtilDebug.logError("Error reading entries from file: " + filePath , e);
        }
        return entries;
    }


    // Save the configuration to a file
    public static void saveLocal(Context context) {
        String filePath = filePathLocal;
        if (null == context) { // TODO do we need context?
            logger.error("Context is null, cannot save local config to file: {}", filePath);
            return;
        }
        if (Entries.entryTree.entries.isEmpty()) {
            logger.warn("------------------------------");
            logger.error("No entries in Entries.entryTree to save to local file: {}", filePath);
            logger.warn("------------------------------");
            return;
        }
        logger.info("Saving local to file: {} ...", filePath);
        logger.debug(" " + (new File(context.getFilesDir(), filePath).getAbsolutePath()));
        // save local-data - with rank
        // extract hidden area /_/ entries
        // make sure they match with Config values
        EntryTree localEntries = new EntryTree();
        Entries.entryTree.entries.forEach((key, entryMap) -> {
            if (key.startsWith("/_/")) {
                localEntries.entries.put(key, entryMap);
            }
        });
        assert localEntries.isEmpty() == false ; // there should be at least config entries
        // make sure config is up to date
        SortedEntryMap localConfigEntries = localEntries.entries.computeIfAbsent(CONFIG_PATH, k -> new SortedEntryMap());
        for (Config config : Config.values()) {
            String configKey = config.getKey();
            String configValue = config.getValue();
            if (null != configValue && !configValue.isEmpty()) {
                // keep existing rank if present ...
                Entry existingEntry = localConfigEntries.get(configKey);
                if (null != existingEntry) {
                    existingEntry.setContent(configValue);
                } else {
                    localConfigEntries.put(configKey, new Entry(configValue));
                }
            }
        }
        //
        Entries.logEntries(localEntries, "Local config entries to save (" + filePath + ")");
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new GZIPOutputStream(context.openFileOutput(filePath, Context.MODE_PRIVATE)))) {
            oos.writeObject(localEntries);
            logger.info("Local saved to file: {}", filePath);
        } catch (Exception e) {
            logger.error("Error saving local to file: {}", filePath, e);
        }
    }

    // Load the configuration from a file
    public static EntryTree loadLocal(Context context) {
        String filePath = filePathLocal;
        ArrayList<String> tenantIds = new ArrayList<>();
        if (!new File(context.getFilesDir(), filePath).exists()) {
            logger.warn("Local file does not exist: {}", filePath);
            return null;
        }
        logger.info("Reading local from file: {} ...", filePath);
        logger.debug(" " + (new File(context.getFilesDir(), filePath).getAbsolutePath()));
        EntryTree localEntries = null;
        try (ObjectInputStream ois = new ObjectInputStream(
                new GZIPInputStream(context.openFileInput(filePath)))) {
            logger.info("Local read from file: {}", filePath);
            localEntries = (EntryTree) ois.readObject();
            Entries.logEntries(localEntries, "Local config entries loaded (" + filePath + ")");
            // load config entries, make sure they are in Config
            SortedEntryMap configEntries = localEntries.entries.getOrDefault(CONFIG_PATH, new SortedEntryMap());
            assert configEntries != null;
            configEntries.forEach((eKey, entry) -> {
                String value = entry.getContent();
                Config config = Config.fromKeyOrNull(eKey);
                if (null != config) {
                    if (null != value && !value.isEmpty()) {
                        if (config == Config.TENANT) {
                            // postpone setting tenantId until all entries are loaded ... as it might trigger reload
                            tenantIds.add(value); // need wrapper to modify from lambda
                        } else {
                            Config.setInternal(config.getKey(), value);
                            logger.debug("Local config loaded: {} -> {}", eKey, value);
                        }
                    } else {
                        logger.error("Local config entry is empty, keep default: {} -> {}", eKey, config.getDefaultValue());
                    }
                } else {
                    logger.warn("Local config key not found in Config enum: {} -> {}", eKey, value);
                }
            });
            // show values below config for debug
            Entries.logEntries(localEntries, "Local config entry loaded and config-checked (" + filePath + ")");
        } catch (Exception e) {
            logger.error("Error loading local from file: {}", filePath, e);
        }
        if (!tenantIds.isEmpty()) {
            String tenantId = tenantIds.get(0); // take first one
            logger.info("Local tenant to load: {}", tenantId);
            Config.TENANT.setValue(tenantId);
        } else {
            logger.warn("No tenantId found in local config");
        }
        // hidden "/_/" entries
        return null != localEntries ? localEntries : new EntryTree();
    }

}