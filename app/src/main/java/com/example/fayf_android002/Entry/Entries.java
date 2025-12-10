package com.example.fayf_android002.Entry;

import android.content.Context;
import com.example.fayf_android002.Config;
import com.example.fayf_android002.MainActivity;
import com.example.fayf_android002.Storage.DataStorageLocal;
import com.example.fayf_android002.Storage.DataStorageWeb;
import com.example.fayf_android002.UI.CustomOnTouchListener;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Singleton
public class Entries {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Entries.class);

    private static Entries instance = new Entries();
    private static Map<String, OnTopicChanged> topicListener = new ConcurrentHashMap<>();
    private static EntryTree entryTree = new EntryTree();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /*
        STATUS - TODO move to ViewModel ?
    */

    private static EntryKey currentEntryKey = EntryTree.ROOT_ENTRY_KEY;

    private static int offset = 0;
    static final int PAGE_SIZE_MIN = 20;
    private static String searchQuery="";

    public static void setSearchQuery(String newText) {
        searchQuery = newText;
    }

    public static CharSequence getSearchQuery() {
        return searchQuery;
    }



    /*
        I N T E R F A C E S
    */

    public interface OnEntriesLoaded {
        void onEntriesLoaded(EntryTree entries);
    }

    public interface OnTopicChanged {
        void onTopicChanged(EntryKey topic);

    }


    /*    TODO move to ViewModel ? */

    private static CustomOnTouchListener viewTouchedInProgress = null;

    public static void registerTouchInProgress(CustomOnTouchListener v) {
        logger.debug("registerTouchInProgress: {} ", v.getId());
        viewTouchedInProgress = v;
    }

    public static void unregisterTouchInProgress(CustomOnTouchListener v) {
        logger.debug("unregisterTouchInProgress: {} reset ", v.getId());
        viewTouchedInProgress = null;
    }

    public static CustomOnTouchListener getViewTouchedInProgress() {
        return viewTouchedInProgress;
    }



    /*
        DEFINITIONS
    */


    // singleton - prevent instantiation
    public Entries() {
    }

    public static Entries getInstance() {
        return instance;
    }



    /* LOAD ENTRIES ASYNC */


    // allow trigger topic change by button click (Fragment1)
    //  as well as back-navigation (top-menu), back-button (MainActivity)
    //  and update mainActivity title as well as Fragment Buttons
    public static void setOnTopicChangedListener(String clientKey, OnTopicChanged listener) {
        // allow multiple listeners - map by clientKey / allow replacing or removing
        if (null == listener) {
            Entries.topicListener.remove(clientKey);
        } else {
            Entries.topicListener.put(clientKey, listener);
        }
    }

    public static void callTopicChangedListeners(EntryKey topic) {

        for (Map.Entry<String, OnTopicChanged> e : topicListener.entrySet()) {
            if ( null != e.getValue()) {
                logger.debug("callTopicChangedListeners: {} for {} ", e.getKey(), Entries.toString(topic) );
                e.getValue().onTopicChanged(topic); // null signals complete reload
            } else {
                logger.debug("callTopicChangedListeners: SKIPP {} has null listener for {} ", e.getKey(), Entries.toString(topic) );
            }
        }
    }

    public static String toString(EntryKey topic) {
        if (null == topic) {
            return "null";
        } else {
            Entry entry = Entries.getEntry(topic);
            return topic.getFullPath() + (null != entry ? " \"" + entry.getContent() + "\"" : " (no entry)");
        }
    }


    /*
        LOAD / SAVE ENTRIES
     */


    private static void load(EntryTree entryTree, Context context, boolean forceWeb) {
        String system = Config.SYSTEM.getValue();
        String tenant = Config.TENANT.getValue();
        if (forceWeb) {
            logger.info("Forcing entries reload from web");
        } else {
            entryTree.set(DataStorageLocal.loadEntries(context));
        }
        if (tenant.endsWith(Config.TENANT_TEST_SUFFIX)) {
            // may have changed
            logger.info("SKIPP load entries async for tenant '{}'", tenant);
        } else if (forceWeb || null == entryTree.entries || entryTree.entries.isEmpty()) {
            entryTree.set(new DataStorageWeb().readData( system, tenant ));
            //
            logger.info("Entries loaded from web ({} entries)", entryTree.entries.size());
            if (entryTree.entries.size() > 0) {
                DataStorageLocal.saveEntries(entryTree, context);
            }
        }
        searchQuery = "";
        offset = 0;
        // check data integrity
        checkDataIntegrity();
        // new DataStorageLocal().saveEntries(entries);
        callTopicChangedListeners(null);
    }

    public static void checkDataIntegrity() {
        EntryTree entryTree = Entries.entryTree;
        // check if current topic exists after load
        if (null == currentEntryKey ||  null == entryTree.get(currentEntryKey)) {
            currentEntryKey = EntryTree.ROOT_ENTRY_KEY;
        }
        // ensure Config entries exist and are changeable
        Config[] configs = Config.values();
        for (Config config : configs) {
            EntryKey configEntryKey = new EntryKey(Config.CONFIG_PATH, config.getKey());
            Entry configEntry = entryTree.get(configEntryKey);
            if (null == configEntry) {
                // create entry with default value
                entryTree.set(configEntryKey, String.valueOf(config.getValue()));
                logger.info("Created missing config entry for {} with default value '{}'"
                        , configEntryKey.getFullPath(), config.getValue());
            } else if (!configEntry.getContent().equals(config.getValue())) {
                // update entry to current value
                configEntry.setContent(config.getValue() );
                logger.warn("Updated config entry for {} to current value '{}'"
                        , configEntryKey.getFullPath(), config.getValue());
            }
        }
        // entry content should have ">"-suffix for topics
        for (Map.Entry<String, TreeMap<String, Entry>> topicEntry : entryTree.entries.entrySet()) {
            //  /
            //      n1 -> c1
            //      n2 -> c2
            //  /n1
            //      n1.1 -> c1.1
            for (Map.Entry<String, Entry> nodeEntry : topicEntry.getValue().entrySet()) {
                EntryKey entryKey = new EntryKey(topicEntry.getKey(), nodeEntry.getKey());
                Entry entry = nodeEntry.getValue();
                checkDataIntegrity(entryKey, entry);
            } // for nodeEntry
        } // for topics
    }


    public static void checkDataIntegrity(EntryKey entryKey, Entry entry) {
        if (null != entryKey && null != entry) {
            boolean isTopic = isTopic(entryKey);
            boolean appearsTopic = entry.getContent().endsWith(">");
            if (isTopic != appearsTopic) {
                if (isTopic) {
                    entry.setContent(entry.getContent().trim() + " >");
                } else {
                    entry.setContent(entry.getContent().substring(0, entry.getContent().length() - 1).trim());
                }
                logger.info("Corrected entry content for {} to '{}'", entryKey.getFullPath(), entry.getContent());
            } // not correct
        }
    }



    public static void load_async(Context context, boolean forceWeb) {
        if (Config.TENANT.getValue().endsWith(Config.TENANT_TEST_SUFFIX)) {
            logger.info("SKIPP load entries async for tenant '{}'", Config.TENANT.getValue());
        } else {
            executorService.execute(() -> {
                try {
                    load(entryTree, context, forceWeb);
                    //logger.info("Entries loaded async ({} entries)", entries.size());
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Error loading entries asynchronously", e);
                }
            });
        } // SKIPP for test tenant
    }

    public static void load_async(Context context) {
        load_async(context, false);
    }




    public static void save(Context context) {
        if (Config.TENANT.getValue().endsWith(Config.TENANT_TEST_SUFFIX)) {
            logger.info("SKIPP save entries async (locale tenant '{}')", Config.TENANT.getValue());
        } else {
            executorService.execute(() -> {
                try {
                    DataStorageLocal.saveEntries(entryTree, context);
                    logger.info("Entries saved async ({} entries)", entryTree.entries.size());
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Error saving entries asynchronously", e);
                }
            });
        }
    }

    /*
        FOR TEST
     */

    public static EntryTree resetEntries() {
        entryTree = new EntryTree();
        checkDataIntegrity(); // ensure config entries exist
        return entryTree;
    }


    /*
        HELPER
     */

    public static void rootTopic() {
        setCurrentEntryKey( EntryTree.ROOT_ENTRY_KEY );
    }

    public static void upOneTopicLevel() {
        // get parent topic -> topic =  fullpath of parent
        EntryKey key = new EntryKey( currentEntryKey.topic);
        if (!isTopic(key)) {
            logger.debug("upOneTopicLevel: current entry is not a topic, go to root");
            key = EntryTree.ROOT_ENTRY_KEY;
        }
        setCurrentEntryKey( key );
    }

    public static EntryKey upOneTopicLevel(EntryKey fromKey) {
        // get parent topic -> topic =  fullpath of parent
        return new EntryKey(fromKey.topic);
    }

    public static boolean changeOffsetBy(int i) {
        // check if more entries exist with current
        int offsetOld = offset;
        offset += i;
        offset = Math.min(offset, getCurrentTopicSize() - PAGE_SIZE_MIN);
        offset = Math.max(0, offset);
        return offsetOld != offset;
    }

    public static Iterator<Map.Entry<String, Entry>> getEntriesIterator(int offset) {
        TreeMap<String, Entry> entry = entryTree.getTopic(currentEntryKey);
        String searchQuery = Entries.searchQuery.startsWith("!") ? Entries.searchQuery.substring(1) : Entries.searchQuery;
        if (null != entry) {
            Iterator<Map.Entry<String, Entry>> iterator = Collections.emptyIterator();
            if (!Entries.searchQuery.startsWith("!")) {
                iterator = entry.entrySet().stream()
                        .filter(e
                                -> searchQuery.isEmpty() || e.getValue().getContent().toLowerCase().contains(searchQuery.toLowerCase()))
                        .skip(offset)
                        .limit(PAGE_SIZE_MIN)
                        .iterator();
            }
            if (!iterator.hasNext() && !searchQuery.isEmpty()) {
                logger.debug("getEntriesIterator: no entries found for topic {} with offset {} and searchQuery '{}'"
                        , Entries.toString(currentEntryKey), offset, searchQuery);
                MainActivity.notifyUser("No local matches, run global search.");
                // search across all topics
                iterator = entryTree.entries.values().stream()
                        .flatMap(m -> m.entrySet().stream())
                        .filter(e
                                -> e.getValue().getContent().toLowerCase().contains(searchQuery.toLowerCase()))
                        .skip(offset)
                        .limit(PAGE_SIZE_MIN)
                        .iterator();
                return iterator;
            }
            return iterator;
        } else {
            return Collections.emptyIterator();
        }
    }




    /*
        GETTERS / SETTERS

     */

    public static void setCurrentEntryKey(EntryKey entryKey) {
        currentEntryKey = null == entryKey ? EntryTree.ROOT_ENTRY_KEY: entryKey;
        offset = 0;
        searchQuery = "";
        callTopicChangedListeners(currentEntryKey);
    }

    public static EntryKey getCurrentEntryKey() {
        if (null == currentEntryKey) {
            currentEntryKey = EntryTree.ROOT_ENTRY_KEY;
        }
        return currentEntryKey;
    }

    public static Entry getCurrentEntry() {
        return getEntry(currentEntryKey);
    }


    public static int getOffset() {
        return offset;
    }


    /*
         Wrapper for EntryTree
     */

    public static Entry getEntry(EntryKey currentTopicEntry) {
        return entryTree.get(currentTopicEntry);
    }

    public static String getContentOr(String configPath, String key, String optional) {
        Entry entry = entryTree.get(new EntryKey(configPath, key));
        if (null != entry) {
            return entry.getContent();
        }
        return optional;
    }


    public static void removeEntry(EntryKey entry) {
        entryTree.remove(entry);
    }

    public static void setEntry(EntryKey entryKey, String content, Context context) {
        assert null != entryKey;
        assert !entryKey.topic.isEmpty();
        assert !entryKey.nodeId.isEmpty();
        assert null != content;
        if (content.isEmpty()){
            logger.info("Removing Entry : {} with empty content ", Entries.toString(entryKey) );
            entryTree.remove(entryKey);
            return;
        }
        Entry entry = entryTree.set(entryKey, content);
        checkDataIntegrity(entryKey, entry);
        // check if first parent
        EntryKey parentKey = Entries.upOneTopicLevel(entryKey);
        if (!parentKey.equals(entryKey) && sizeTopic(parentKey) < 2) {
            // first child in topic -> check parent integrity
            checkDataIntegrity(parentKey, Entries.getEntry(parentKey));
        }
        if (Config.TENANT.getValue().endsWith(Config.TENANT_TEST_SUFFIX)) {
            logger.info("SKIPP upload Entry (volatile tenant {} ) : {} with \"{}\" ", Config.TENANT, Entries.toString(entryKey), content);
        } else if (entryKey.topic.startsWith(Config.CONFIG_PATH)) {
            logger.debug("SKIPP upload Config Entry : {} with \"{}\" ", Entries.toString(entryKey), content);
        } else {
            new DataStorageWeb().saveEntry(entryKey, entry);
            logger.info("Uploaded Entry : {} with \"{}\" ", Entries.toString(entryKey), content);
        }
    }

    public static Entries setEntry(String topic, String nodeId, String content) {
        entryTree.set(new EntryKey(topic, nodeId), content);
        return Entries.getInstance();
    }


    public static int size() {
        return entryTree.size();
    }

    public static int getCurrentTopicSize() {
        TreeMap<String, Entry> topicEntries = entryTree.getTopic(currentEntryKey);
        return null != topicEntries ? topicEntries.size() : 0;
    }

    public static int sizeTopic(EntryKey entryKey) {
        TreeMap<String, Entry> topicEntries = entryTree.getTopic(entryKey);
        return null != topicEntries ? topicEntries.size() : 0;
    }

    public static boolean isTopic(EntryKey entryKey) {
        return entryTree.getTopic(entryKey) != null;
    }

}
