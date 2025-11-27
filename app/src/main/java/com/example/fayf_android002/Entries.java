package com.example.fayf_android002;

import android.content.Context;
import android.view.View;
import com.example.fayf_android002.Storage.DataStorageLocal;
import com.example.fayf_android002.Storage.DataStorageWeb;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.fayf_android002.Entry.DELETION_SUFFIX;

// Singleton
public class Entries {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Entries.class);


    public interface OnEntriesLoaded {
        void onEntriesLoaded(EntryTree entries);
    }
    public interface OnTopicChanged {
        void onTopicChanged(Entry topic);
    }

    private static Entries instance = new Entries();
    private static OnEntriesLoaded listener;
    private static Map<String, OnTopicChanged> topicListener = new ConcurrentHashMap<>();
    private static EntryTree entryTree = new EntryTree();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /*
        STATUS
     */

    private static Entry currentEntry = null;
    private static Entry currentTopicEntry = null;

    private static List<Entry> recentEntries = new ArrayList<>();
    private static int offset = 0;
    private static int currentTopicEntryCount;

    static final int PAGE_SIZE_MIN = 5;

    public static final String ROOT_TOPIC = "/";







    // singleton - prevent instantiation
    public Entries() {
    }

    public static Entries getInstance() {
        return instance;
    }


    /*    TODO move to ViewModel ? */

    private static View viewTouchedInProgress = null;

    public static void settingTouchInProgress(View v) {
        logger.debug("settingTouchInProgress: {} ", v.getId());
        viewTouchedInProgress = v;
    }
    public static void settingTouchInProgressReset(View v) {
        logger.debug("settingTouchInProgress: {} reset ", v.getId());
        viewTouchedInProgress = null;
    }
    public static View getViewTouchedInProgress() {
        return viewTouchedInProgress;
    }






    /* LOAD ENTRIES ASYNC */

    public static void setOnEntriesLoadedListener(OnEntriesLoaded listener) {
        Entries.listener = listener;
    }

    // allow trigger topic change by button click (Fragment1)
    //  as well as back-navigation (top-menu), back-button (MainActivity)
    //  and update mainActivity title as well as Fragment Buttons
    public static void setOnTopicChangedListener(String clientKey, OnTopicChanged listener) {
        // allow multiple listeners - map by clientKey / allow replacing or removing
        if (null == listener) {
            Entries.topicListener.remove(clientKey);
        } else {
            Entries.topicListener.put( clientKey, listener);
        }
    }

    public static void callTopicChangedListeners(Entry topic) {

        if (topicListener != null && null != topic) {
            for (OnTopicChanged listener : topicListener.values()) {
                if (listener != null)
                    listener.onTopicChanged(topic);
            }
        }
    }


    /*
        LOAD / SAVE ENTRIES
     */




    public static void load(EntryTree entryTree, Context context, boolean forceWeb) {
        if (forceWeb) {
            logger.info("Forcing entries reload from web");
            entryTree.entries = null; // force reload from web
        } else  {
            entryTree.entries = DataStorageLocal.loadEntries( context);
        }
        if (null == entryTree.entries || entryTree.entries.isEmpty()) {
            List<String> strings = new DataStorageWeb().readData();
            if (null == entryTree.entries) {
                entryTree.entries = new TreeMap<>();
            }
            for (String line : strings) {
                Entry entry = Entry.build(line);
                if (entry != null) {
                    if (entry.content.endsWith(DELETION_SUFFIX)) {
                        entryTree.removeEntry(entry);
                    } else {
                        entryTree.addEntry(entry.topic, entry);
                    }
                }
            }
            logger.info("Entries loaded from web ({} entries)", entryTree.entries.size());
            DataStorageLocal.saveEntries(entryTree.entries,  context);
        }
        // ensure config is merged
        // config entry must exist - even if not in storage
        Entries.entryTree.entries.putIfAbsent(Entry.HIDDEN_ENTRY_PATH, new TreeMap<>());
        Entries.entryTree.setEntry( new Entry(Config.CONFIG_PATH, "Config"), false);

        // at least 1 entry to allow navigation to config
        Entries.entryTree.setEntry( new Entry(Config.CONFIG_PATH,"Version", "Version : 0.0.1"), false);
        Entries.entryTree.setEntry( new Entry(Config.CONFIG_PATH,"Tenant", "Tenant : " + Config.TENANT.getValue() ), false);

        // new DataStorageLocal().saveEntries(entries);
        callTopicChangedListeners(currentTopicEntry);
        if (listener != null) {
            listener.onEntriesLoaded(entryTree);
        }
    }

    public static void load_async(Context context , boolean forceWeb) {
        executorService.execute(() -> {
            try {
                load(entryTree, context, forceWeb);
                //logger.info("Entries loaded async ({} entries)", entries.size());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error loading entries asynchronously", e);
            }
        });
    }

    public static void load_async(Context contextm ) {
        load_async( contextm, false);
    }


    public static void save(Context context) {
        executorService.execute(() -> {
            try {
                DataStorageLocal.saveEntries(entryTree.entries, context);
                logger.info("Entries saved async ({} entries)", entryTree.entries.size());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error saving entries asynchronously", e);
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

    public static Entry getEntryOrNew(String entryParentPath, String nodeId, String defaultContent) {
        Entry entry = entryTree.getEntry(entryParentPath, nodeId);
        // TODO do not use NULL_ENTRY as DUMMY-ENTRY
        if (entry == null || EntryTree.NULL_ENTRY.equals(entry) /*DUMMY-ENTRY*/)  {
            entry = new Entry(entryParentPath, nodeId , defaultContent);
            logger.info("New entry created: {}", entry.getFullPath());
            entryTree.setEntry(entry, true); // add new entry to tree
        }
        return entry;
    }

    public static Entry createNewChildEntry(Entry parentEntry, String content) {
         String nodeId = String.valueOf(System.currentTimeMillis());
         Entry entry = new Entry(parentEntry.getFullPath(), nodeId, content);
         logger.info("New entry created: {}", entry.getFullPath());
         entryTree.setEntry(entry, true); // add new entry to tree
         return entry;
    }



    public static boolean isValidTopic(Entry entry) {
        return entryTree.entries.containsKey(entry.getFullPath());
    }

    public static void removeEntry(Entry entry) {
         entry.content += DELETION_SUFFIX; //mark as deleted
         persistEntry(entry);
         entryTree.removeEntry(entry);
         logger.info("Entry removed: {}", entry.getFullPath());
    }


    public static void setContent(Entry entry, String newContent, Context context) {
         if (evaluateEntryUpdate(entry, newContent)) {
                if ("/_/config/Tenant".equals(entry.getFullPath())){
                    // special handling of tenant change
                    String oldTenant = getTenant();
                    String newTenant = entry.getContent().replace("Tenant : ", "").trim();
                    logger.info("Tenant change detected: {} -> {}", oldTenant, newTenant);
                    if (null == context) {
                        logger.warn("Context is null - cannot reload entries for new tenant");
                        return;
                    } else if (!oldTenant.equals(newTenant)) {
                        entryTree = new EntryTree(); // reset
                        Config.TENANT.setValue( newTenant);
                        load_async( context , true); // force web reload
                    }
                } else {
                    persistEntry(entry);
                }
         }
        logger.info("Entry content updated: {}", entry.getFullPath());
    }





    private static void persistEntry(Entry entry) {
        //logger.info("Entries loaded async ({} entries)", entries.size());
        // new Entries not in tree yet -> update before returning to first fragment
        entryTree.addEntryIfNew(entry);
        //
        if (!entry.getTopic().startsWith(Entry.HIDDEN_ENTRY_PATH+Entry.PATH_SEPARATOR)) {
            executorService.execute(() -> {
                try {
                    new DataStorageWeb().saveEntry(entry);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Error persisting entry asynchronously: {}", entry.getFullPath(), e);
                }
            });
        } else {
            logger.info("Skipped uploading Settings: {}", entry.getFullPath());
        }
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
        Entry entry = currentTopicEntry;
        // might be init -> got to "/" - main
        // might be regular topic -> go to parent
        // might be root "/" -> stay at "/"
        // might be hidden topic -> do not move to hidden-base-root /_/
        String currentPath = entry != null ? entry.getFullPath() : "/";
        String parentTopic = Entry.getTopicFromFullPath(currentPath);
        if (!parentTopic.equals(currentPath) && ! parentTopic.equals(Entry.HIDDEN_ENTRY_PATH+Entry.PATH_SEPARATOR)) {
            entry = getEntryOrNew(parentTopic);
            setTopicEntry(entry);
        }
        return entry;
    }


    /*
       ITERATORS
     */

    public static Iterator<Map.Entry<String, Entry>> getEntriesIterator(String topic, int offset) {
        return null == entryTree || null == entryTree.entries ? Collections.emptyIterator() :  entryTree.getEntriesIterator(topic, offset);
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

    public static boolean setTopicEntry(Entry topic) {
        // check if entry is topic or leaf or not allowed
        boolean valid = isValidTopic(topic);
        if  (valid){
            if (currentTopicEntry != null && !currentTopicEntry.equals(topic)) {
                recentEntries.add(currentTopicEntry);
            }
            currentTopicEntry = topic;
            currentEntry = null; // reset current entry when topic changes
            currentTopicEntryCount = entryTree.size(currentTopicEntry);
            callTopicChangedListeners(topic);
        } else {
            logger.warn("Skipped to set leaf/invalid topic entry: {}", topic.getFullPath());
        }
        return valid;
    }

    public static List<Entry> getRecentEntries() {
        return recentEntries;
    }

    public static String getTenant(){
        String tenantName = "";
        Entry entry = entryTree.getEntry(Entry.TENANT_ENTRY_PATH);
        if (entry == null || EntryTree.NULL_ENTRY.equals(entry)) {
            tenantName = Config.TENANT.getValue();
        } else {
            tenantName = entry.getContent();
        }        
        return tenantName;
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


    public static boolean changeOffsetBy(int i) {
        // check if more entries exist with current
        int offsetOld = offset;
        offset += i;
        offset = Math.min(offset, currentTopicEntryCount - PAGE_SIZE_MIN);
        offset = Math.max(0, offset);
        return offsetOld != offset;
    }


    /*
        FOR TESTING

     */



}
