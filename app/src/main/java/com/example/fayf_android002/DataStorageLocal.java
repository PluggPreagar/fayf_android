package com.example.fayf_android002;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class DataStorageLocal {

    private static final Logger logger = LoggerFactory.getLogger(DataStorageLocal.class);

    // save Entries to persistent storage
    public void saveEntries(EntryTree entries) {
        try (FileOutputStream fos = new FileOutputStream("entries.dat");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(entries);
            logger.info("Entries saved successfully: {}", entries);
        } catch (IOException e) {
            logger.error("Failed to save entries: {}", entries, e);
        }
    }

    public EntryTree loadEntries() {
        try (FileInputStream fis = new FileInputStream("entries.dat");
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            EntryTree entries = (EntryTree) ois.readObject();
            logger.info("Entries loaded successfully: {}", entries);
            return entries;
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to load entries.", e);
        }
        return new EntryTree();
    }


}