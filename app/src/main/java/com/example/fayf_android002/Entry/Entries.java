package com.example.fayf_android002.Entry;

import android.content.Context;
import android.view.View;
import com.example.fayf_android002.Config;
import com.example.fayf_android002.Storage.DataStorageLocal;
import com.example.fayf_android002.Storage.DataStorageWeb;

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
    private static OnEntriesLoaded listener;
    private static Map<String, OnTopicChanged> topicListener = new ConcurrentHashMap<>();
    private static EntryTree entryTree = new EntryTree();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /*
        STATUS - TODO move to ViewModel ?
    */

    private static EntryKey currentEntryKey = EntryTree.ROOT_ENTRY_KEY;
    private static int currentTopicEntryCount; // for offset

    private static int offset = 0;
    static final int PAGE_SIZE_MIN = 5;



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
            Entries.topicListener.put(clientKey, listener);
        }
    }

    public static void callTopicChangedListeners(EntryKey topic) {

        for (OnTopicChanged listener : topicListener.values()) {
            listener.onTopicChanged(topic); // null signals complete reload
        }
    }


    /*
        LOAD / SAVE ENTRIES
     */


    public static void load(EntryTree entryTree, Context context, boolean forceWeb) {
        if (forceWeb) {
            logger.info("Forcing entries reload from web");
        } else {
            entryTree.set(DataStorageLocal.loadEntries(context));
        }
        if (forceWeb || null == entryTree.entries || entryTree.entries.isEmpty()) {
            entryTree.set(new DataStorageWeb().readData());
            // keep config defaults
            for (Config config : Config.values()) {
                Config.set(config.getKey(), config.getValue());
            }
            //
            logger.info("Entries loaded from web ({} entries)", entryTree.entries.size());
            DataStorageLocal.saveEntries(entryTree, context);
        }

        // check if current topic exists after load
        if (null == entryTree.get(currentEntryKey)) {
            currentEntryKey = EntryTree.ROOT_ENTRY_KEY;
        }

        // new DataStorageLocal().saveEntries(entries);
        callTopicChangedListeners(null);
        if (listener != null) {
            listener.onEntriesLoaded(entryTree);
        }
    }

    public static void load_async(Context context, boolean forceWeb) {
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

    public static void load_async(Context context) {
        load_async(context, false);
    }


    public static void save(Context context) {
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

    /*
        FOR TEST
     */

    public static EntryTree clearAllEntries() {
        entryTree = new EntryTree();
        return entryTree;
    }


    /*
        HELPER
     */

    public static void upOneTopicLevel() {
        // get parent topic -> topic =  fullpath of parent
        EntryKey key = new EntryKey( currentEntryKey.topic);
        setCurrentEntryKey( key );
    }

    public static boolean changeOffsetBy(int i) {
        // check if more entries exist with current
        int offsetOld = offset;
        offset += i;
        offset = Math.min(offset, currentTopicEntryCount - PAGE_SIZE_MIN);
        offset = Math.max(0, offset);
        return offsetOld != offset;
    }

    public static Iterator<Map.Entry<String, Entry>> getEntriesIterator(int offset) {
        TreeMap<String, Entry> entry = entryTree.getTopic(currentEntryKey);
        if (null != entry) {
            currentTopicEntryCount = entry.size();
            return entry.entrySet().stream()
                    .skip(offset)
                    .limit(PAGE_SIZE_MIN)
                    .iterator();
        } else {
            currentTopicEntryCount = 0;
            return Collections.emptyIterator();
        }
    }




    /*
        GETTERS / SETTERS

     */

    public static void setCurrentEntryKey(EntryKey entryKey) {
        currentEntryKey = null == entryKey ? entryKey : EntryTree.ROOT_ENTRY_KEY;
        offset = 0;
        callTopicChangedListeners(null);
        if (listener != null) {
            listener.onEntriesLoaded(entryTree);
        }
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
        Entry entry = entryTree.set(entryKey, content);
        new DataStorageWeb().saveEntry(entryKey, entry);
    }

    public static Entries setEntry(String topic, String nodeId, String content) {
        entryTree.set(new EntryKey(topic, nodeId), content);
        return Entries.getInstance();
    }


    public static int size() {
        return entryTree.size();
    }



}
