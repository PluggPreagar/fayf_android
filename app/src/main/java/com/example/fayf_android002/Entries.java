package com.example.fayf_android002;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.fayf_android002.Entry.DELETION_SUFFIX;

// Singleton
public class Entries {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Entries.class);


    public interface OnEntriesLoadedListener {
        void onEntriesLoaded(EntryTree entries);
    }

    private static Entries instance = new Entries();
    private static OnEntriesLoadedListener listener;
    private static EntryTree entryTree = new EntryTree();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /*
        STATUS
     */

    private static Entry currentEntry = null;
    private static Entry currentTopicEntry = null;

    private static List<Entry> recentEntries = new ArrayList<>();
    private static int offset = 0;




    // singleton - prevent instantiation
    public Entries() {
    }

    public static Entries getInstance() {
        return instance;
    }


    /* LOAD ENTRIES ASYNC */

    public static void setOnEntriesLoadedListener(OnEntriesLoadedListener listener) {
        Entries.listener = listener;
    }

    public static void load(EntryTree entries){
        List<String> strings = new DataStorageWeb().readData();
        for (String line : strings) {
            Entry entry = Entry.build(line);
            if (entry != null) {
                if (entry.content.endsWith(DELETION_SUFFIX)) {
                    entries.removeEntry(entry);
                } else {
                    entries.addEntry(entry.topic, entry);
                }
            }
        }
        logger.info("Entries loaded async ({} entries)", entries.size());
        // new DataStorageLocal().saveEntries(entries);
        if (listener != null) {
            listener.onEntriesLoaded(entries);
        }
    }

    public static void load_async(){
        executorService.execute(() -> {
            try {
                load(entryTree);
                //logger.info("Entries loaded async ({} entries)", entries.size());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error loading entries asynchronously", e);
            }
        });
    }

    /*
        HELPER
     */

     public EntryTree getEntryTree() {
         return entryTree;
     }

    public static Entry getEntry(String entryFullPath) {
         // used during navigation to edit screen - can handover only a string
         return entryTree.getEntry(entryFullPath);
    }

    public static Entry getEntryOrNew(String entryFullPath) {
            Entry entry = getEntry(entryFullPath);
            if (entry == null || EntryTree.NULL_ENTRY.equals(entry) /*DUMMY-ENTRY*/)  {
                entry = new Entry(Entry.getTopicFromFullPath(entryFullPath)
                        , Entry.getNodeIdFromFullPath(entryFullPath), "");
            }
            return entry;
    }


    public static void removeEntry(Entry entry) {
         entry.content += DELETION_SUFFIX; //mark as deleted
         persistEntry(entry);
         entryTree.removeEntry(entry);
         logger.info("Entry removed: {}", entry.getFullPath());
    }


    public static void setContent(Entry entry, String newContent) {
         if (evaluateEntryUpdate(entry, newContent)) {
                persistEntry(entry);
         }
        logger.info("Entry content updated: {}", entry.getFullPath());
    }

    private static void persistEntry(Entry entry) {
        executorService.execute(() -> {
            try {
                new DataStorageWeb().saveEntry(entry);
                //logger.info("Entries loaded async ({} entries)", entries.size());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error persisting entry asynchronously: {}", entry.getFullPath(), e);
            }
        });

    }

    private static boolean evaluateEntryUpdate(Entry entry, String newContent) {
        // TODO implement check or keeping prefix etc.
        boolean isContentChanged = !entry.content.equals(newContent);
        entry.content = newContent;
        logger.info("Entry content update evaluation passed: {}", entry.getFullPath());
        return isContentChanged;
    }


    public static String newEntryPath(String topic) {
         // create Id from unix timestamp
        String id = String.valueOf(System.currentTimeMillis());
        return topic + "/" + id ;
    }


    public static Entry restoreLastTopic() {
        Entry entry = null;
        if (!recentEntries.isEmpty()) {
            entry = recentEntries.remove(recentEntries.size() - 1);// last entry
        } else {
            entry = getEntryOrNew("/"); // root
        }
        currentTopicEntry = entry;
        return entry;
    }

    public static Entry moveUpOneTopicLevel() {
        Entry entry = null;
        if (currentTopicEntry != null) {
            String parentTopic = Entry.getTopicFromFullPath(currentTopicEntry.getTopic());
            entry = getEntryOrNew(parentTopic);
            currentTopicEntry = entry;
        }
        return entry;
    }


    /*
       ITERATORS
     */

    public static Iterator<Map.Entry<String, Entry>> getEntriesIterator(String topic, int offset) {
        return getInstance().getEntryTree().getEntriesIterator(topic, offset);
    }


    /*
        GETTER / SETTER
     */

    public static Entry getCurrentEntry() {
        return currentEntry;
    }
    public static void setCurrentEntry(Entry entry) {
        currentEntry = entry;
    }

    public static Entry getCurrentTopicEntry() {
        return currentTopicEntry;
    }

    public static void setTopicEntry(Entry topic) {
        if (currentTopicEntry != null && !currentTopicEntry.equals(topic)) {
            recentEntries.add(currentTopicEntry);
        }
        currentTopicEntry = topic;
    }

    public static List<Entry> getRecentEntries() {
        return recentEntries;
    }

    public static int getOffset() {
        return offset;
    }

    public static void setOffset(int offset) {
        Entries.offset = offset;
    }

    public static String getCurrentTopicString() {
        if (currentTopicEntry != null) {
            // Entry can act as Topic - topic = parents full path
            return currentTopicEntry.getFullPath(); // Full path as topic
        } else {
            return "/";
        }
    }

}
