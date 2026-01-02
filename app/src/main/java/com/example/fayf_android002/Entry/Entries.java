package com.example.fayf_android002.Entry;

import android.content.Context;
import com.example.fayf_android002.Config;
import com.example.fayf_android002.MainActivity;
import com.example.fayf_android002.RuntimeTest.RuntimeChecker;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import com.example.fayf_android002.IO.IOLocal;
import com.example.fayf_android002.IO.IOWeb;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Singleton
public class Entries {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Entries.class);

    private static Entries instance = new Entries();
    private static Map<String, OnTopicChanged> topicListener = new ConcurrentHashMap<>();
    private static Map<String, OnDataChanged> dataListener = new ConcurrentHashMap<>();
    public static EntryTree entryTree = new EntryTree();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /*
        STATUS - TODO move to ViewModel ?
    */

    private static EntryKey currentEntryKey = EntryTree.ROOT_ENTRY_KEY;

    private static int offset = 0;
    static final int PAGE_SIZE_MIN = 20;
    private static String searchQuery="";
    private static long lastSaveTimeMs = 0;
    private static boolean saveRunning = false;
    private static long entryModified = 0 ; // update marker

    public static void setSearchQuery(String newText) {
        searchQuery = newText;
    }

    public static CharSequence getSearchQuery() {
        return searchQuery;
    }

    public static void entryModified() {
        if ( entryModified <= 0) { // first time
            entryModified = System.currentTimeMillis();
        }
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

    public interface OnDataChanged {

        enum ChangeType {
            ENTRY_RANK_CHANGED,
            TOPIC_CHANGED,
        }
        void onDataChanged(EntryKey entryKey, ChangeType changeType);
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
        UtilDebug.logCompactCallStack("callTopicChangedListeners");
        for (Map.Entry<String, OnTopicChanged> e : topicListener.entrySet()) {
            if ( null != e.getValue()) {
                logger.debug("callTopicChangedListeners: {} for {} ", e.getKey(), Entries.toString(topic) );
                e.getValue().onTopicChanged(topic); // null signals complete reload
            } else {
                logger.debug("callTopicChangedListeners: SKIPP {} has null listener for {} ", e.getKey(), Entries.toString(topic) );
            }
        }
    }

    /* Data Changed - just refresh, but keep offset and topic */

    public static void setOnDataChangedListener(String clientKey, OnDataChanged listener) {
        // allow multiple listeners - map by clientKey / allow replacing or removing
        if (null == listener) {
            Entries.dataListener.remove(clientKey);
        } else {
            Entries.dataListener.put(clientKey, listener);
        }
    }

    public static void callDataChangedListeners(EntryKey topic, OnDataChanged.ChangeType changeType) {

        for (Map.Entry<String, OnDataChanged> e : dataListener.entrySet()) {
            if ( null != e.getValue()) {
                logger.debug("callDataChangedListeners: {} for {} ", e.getKey(), Entries.toString(topic) );
                e.getValue().onDataChanged(topic, changeType); // null signals complete reload
            } else {
                logger.debug("callDataChangedListeners: SKIPP {} has null listener for {} ", e.getKey(), Entries.toString(topic) );
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
        if (null == context) {
            logger.error("Entries.load: context is null, cannot load entries");
            return;
        }
        RuntimeChecker.check();
        String system = Config.SYSTEM.getValue();
        String tenant = Config.TENANT.getValue();
        boolean fromWeb = false;
        // keep my rankings and votes ?
        SortedEntryTreeMap entryTreeOld = Entries.entryTree.entries;
        if (forceWeb) {
            logger.info("Forcing entries reload from web");
        } else {
            entryTree.setData(IOLocal.loadConfig(context));
        }
        if (tenant.endsWith(Config.TENANT_TEST_SUFFIX)) {
            // may have changed
            logger.info("SKIPP load entries async for tenant '{}'", tenant);
        } else if (forceWeb || 0 == Entries.sizeTopic(EntryTree.ROOT_ENTRY_KEY) ) {
            entryTree.setData(IOWeb.readData( system, tenant ));
            Entries.logEntries(entryTree, "Entries loaded from web");
            logger.info("Entries loaded from web ({} entries)", entryTree.entries.size());
            fromWeb = true;
        }
        RuntimeChecker.check();
        searchQuery = "";
        offset = 0;
        checkDataIntegrity();
        callTopicChangedListeners(null);
        Entries.logEntries(entryTree, "finished loading and integrity check");
        if (fromWeb){
            IOLocal.saveData(entryTree, context);
        }
        RuntimeChecker.check();
    }


    public static void loadDelta() {
        EntryTree deltaTree = IOWeb.readDataDelta(
                Config.SYSTEM.getValue()
                , Config.TENANT.getValue()
                , null);
        boolean currentTopicUpdated = EntryTree.merge(entryTree, deltaTree);
        if (IOWeb.lastDeltaCount > 0) {
            checkDataIntegrity();
            MainActivity.userInfo("Loaded " + IOWeb.lastDeltaCount + " updates.");
        }
        logger.info("Entries loaded delta up to {} ({} updates)", IOWeb.lastTimestamp, IOWeb.lastDeltaCount);
        Config.LAST_DATA_TIMESTAMP.setValue( IOWeb.lastTimestamp );
        // current TimeStamp
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String ts = formatter.format(new Date());
        Config.LAST_SYNC_TIMESTAMP.setValue( ts );
        if (currentTopicUpdated) {
            Entries.sortCurrentTopic();
            callTopicChangedListeners(getCurrentEntryKey());
        }
    }


    public static void checkDataIntegrity() {
        UtilDebug.logCompactCallStack("checkDataIntegrity");
        EntryTree entryTree = Entries.entryTree;
        // check if current topic exists after load
        if (null == currentEntryKey ||  null == entryTree.get(currentEntryKey)) {
            currentEntryKey = EntryTree.ROOT_ENTRY_KEY;
        }
        // ensure Config entries exist and are changeable
        Config[] configs = Config.values();
        RuntimeChecker.check();
        for (Config config : configs) {
            EntryKey configEntryKey = new EntryKey(Config.CONFIG_PATH, config.getKey());
            Entry configEntry = entryTree.get(configEntryKey);
            if (null == configEntry) {
                // create entry with default value
                entryTree.setData(configEntryKey, String.valueOf(config.getValue()), false);
                logger.info("Created missing config entry for {} with default value '{}'"
                        , configEntryKey.getFullPath(), config.getValue());
            } else if (!configEntry.getContent().equals(config.getValue())) {
                // update entry to current value
                configEntry.setContent(config.getValue() );
                logger.warn("Updated config entry for {} to current value '{}'"
                        , configEntryKey.getFullPath(), config.getValue());
            }
        }
        RuntimeChecker.check();
        // entry content should have ">"-suffix for topics
        for (Map.Entry<String, SortedEntryMap> topicEntry : entryTree.entries.entrySet()) {
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
        //
         // check for wrong quoted topics -> merge with correct ones
        List<String> wrongTopics = new ArrayList<>();
        for (Map.Entry<String, SortedEntryMap> topicEntry : entryTree.entries.entrySet()){
            String topic = topicEntry.getKey();
            if (topic.startsWith("/\"")) {
                String correctTopic = topic.substring(2);
                wrongTopics.add(topic);
                // might create Topic
                SortedEntryMap correctSortedEntryMap = entryTree.entries.getOrDefault(correctTopic, new SortedEntryMap());
                topicEntry.getValue().forEach( (nodeId, entry) -> {
                            logger.info("Found entry '{}' in wrong quoted topic '{}'", nodeId, topic);
                            correctSortedEntryMap.put(nodeId, entry);
                        }
                );
            }
        } // for topics
        // remove wrong quoted topics
        for (String wrongTopic : wrongTopics) {
            entryTree.entries.remove(wrongTopic);
            logger.info("Removed wrong quoted topic '{}'", wrongTopic);
        }
        RuntimeChecker.check();

    }


    public static void checkDataIntegrity(EntryKey entryKey, Entry entry) {
        if (null != entryKey && null != entry) {
            boolean isTopic = isTopic(entryKey);
            boolean appearsTopic = entry.getContent().endsWith(">");
            if (isTopic != appearsTopic) {
                if (isTopic) {
                    // do not add - will create issue with config entries
                    // just add on display
                    // entry.setContent(entry.getContent().trim() + " >");
                } else {
                    entry.setContent(entry.getContent().substring(0, entry.getContent().length() - 1).trim());
                }
                logger.info("Corrected entry content for {} to '{}'", entryKey.getFullPath(), entry.getContent());
            } // not correct
        }
    }



    public static void loadAsync(Context context, boolean forceWeb) {
        if (Config.TENANT.getValue().endsWith(Config.TENANT_TEST_SUFFIX)) {
            logger.info("SKIPP load entries async for tenant '{}'!", Config.TENANT.getValue());
            logger.warn("SKIPP load entries async for tenant '{}'!", Config.TENANT.getValue());
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

    public static void loadAsync(Context context) {
        loadAsync(context, false);
    }


    public static void loadConfig(Context context) {
        if (null == context) {
            logger.error("Entries.loadConfig: context is null, cannot load config entries");
            return;
        }
        // not async - must be available before anything else
        EntryTree entryTreeLoaded = IOLocal.loadLocal(context);// load config before anything else
        RuntimeChecker.check();
        entryTree.setConfig(entryTreeLoaded);
        RuntimeChecker.check();
        checkDataIntegrity();
    }

    public static void save(Context context) {
        save(context, null);
    }


    public static void save(Context context, EntryKey entryKey) {
        if (lastSaveTimeMs + 2000 > System.currentTimeMillis()) {
            logger.info("SKIPP save entries - last save was less than 2s ago");
            return;
        }
        lastSaveTimeMs = System.currentTimeMillis(); //KLUDGE
        saveRunning = true; // KLUDGE for testing - onStop-MainActivity
        if (null == context) {
            logger.error("Entries.save: context is null, cannot save entries");
            return;
        }
        if (Config.TENANT.getValue().endsWith(Config.TENANT_TEST_SUFFIX)) {
            logger.info("SKIPP save entries async (locale tenant '{}')", Config.TENANT.getValue());
        } else {
            UtilDebug.logCompactCallStack("save entries async");
            executorService.execute(() -> {
                try {
                    if (null == entryKey ||entryKey.topic.startsWith("/_/")) {
                        IOLocal.saveConfig(context); // save config first
                    }
                    if (null == entryKey || !entryKey.topic.startsWith("/_/")) {
                        IOLocal.saveData(entryTree, context);
                    }
                    if (null == entryKey){
                        entryModified = 0; // reset modification marker
                    }
                    logger.info("Entries saved async ({} entries)", entryTree.entries.size());
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Error saving entries asynchronously", e);
                }
            });
        }
        saveRunning = false;
    }

    public static boolean isSaveRunning() {
        return saveRunning;
    }

    /*
        FOR TEST
     */


    public static EntryTree resetEntries(boolean keepConfig) {
        if (keepConfig) {
            logger.warn("Resetting all entries but keeping config");
            EntryTree.filterConfig(entryTree);
        } else {
            logger.warn("Resetting all entries");
            entryTree = new EntryTree(); // keep config ...
        }
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
        if (!isTopic(key) || sizeTopic(key) < 1) {
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

    public static SortedEntryMap getTopicEntries(){
        // TODO optimize - avoid double iteration
        Iterator<Map.Entry<String, Entry>> entriesIterator = getEntriesIterator(0);
        SortedEntryMap entries = new SortedEntryMap();
        while (entriesIterator.hasNext()) {
            Map.Entry<String, Entry> e = entriesIterator.next();
            entries.put(e.getKey(), e.getValue());
        }
        return entries;
    }

    public static Iterator<Map.Entry<String, Entry>> getEntriesIterator(int offset) {
        SortedEntryMap entry = entryTree.getTopic(currentEntryKey);
        String searchQuery = Entries.searchQuery.startsWith("!") ? Entries.searchQuery.substring(1) : Entries.searchQuery;
        if (null != entry) {
            Iterator<Map.Entry<String, Entry>> iterator = Collections.emptyIterator();
            if (!Entries.searchQuery.startsWith("!")) {
                iterator = entry.entrySet().stream()
                        .filter( e -> EntryStyle.getByContent(e.getValue().getContent()).isEnabled() )
                        .filter(e
                                -> searchQuery.isEmpty() || e.getValue().getContent().toLowerCase().contains(searchQuery.toLowerCase()))
                        .skip(offset)
                        .limit(PAGE_SIZE_MIN)
                        .iterator();
            }
            if (!iterator.hasNext() && !searchQuery.isEmpty()) {
                logger.debug("getEntriesIterator: no entries found for topic {} with offset {} and searchQuery '{}'"
                        , Entries.toString(currentEntryKey), offset, searchQuery);
                MainActivity.userInfo("No local matches, run global search.");
                // search across all topics
                iterator = entryTree.entries.values().stream()
                        .flatMap(m -> m.entrySet().stream())
                        .filter( e -> EntryStyle.getByContent(e.getValue().getContent()).isEnabled() )
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
        EntryKey oldKey = currentEntryKey;
        currentEntryKey = null == entryKey ? EntryTree.ROOT_ENTRY_KEY: entryKey;
        offset = 0;
        if (null == oldKey || !oldKey.equals(currentEntryKey) || EntryTree.isSortingInvalid() ) {
            searchQuery = ""; // prevent reset of search on changing search filter
            sortCurrentTopic();
        }
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
        if (entryKey.topic.startsWith("/\"")) {
            logger.warn("FIX Invalid topic in EntryKey: {} ", entryKey);
            entryKey.topic = entryKey.topic.substring(2);
        }
        Entry entry = entryTree.setData(entryKey, content);
        checkDataIntegrity(entryKey, entry);
        // check if first parent
        EntryKey parentKey = Entries.upOneTopicLevel(entryKey);
        if (!parentKey.equals(entryKey) && sizeTopic(parentKey) < 2) {
            // first child in topic -> check parent integrity
            checkDataIntegrity(parentKey, Entries.getEntry(parentKey));
        }
        sendEntry(entryKey, content, entry);
    }

    private static void sendEntry(EntryKey entryKey, String content, Entry entry) {
        if (Config.TENANT.getValue().endsWith(Config.TENANT_TEST_SUFFIX)) {
            logger.info("SKIPP upload Entry (volatile tenant {} ) : {} with \"{}\" ", Config.TENANT, Entries.toString(entryKey), content);
        } else if (entryKey.topic.startsWith(Config.CONFIG_PATH)) {
            logger.debug("SKIPP upload Config Entry : {} with \"{}\" ", Entries.toString(entryKey), content);
        } else {
            save(MainActivity.getContext(), entryKey);
            new IOWeb().saveEntry(entryKey, entry);
            logger.info("Uploaded Entry : {} with \"{}\" ", Entries.toString(entryKey), content);
        }
    }

    public static Entries setEntryInternal(String topic, String nodeId, String content) {
        entryTree.setData(new EntryKey(topic, nodeId), content); // TODO cleanup different version of setEntry
        return Entries.getInstance();
    }


    public static int size() {
        return entryTree.size();
    }

    public static int getCurrentTopicSize() {
        SortedEntryMap topicEntries = entryTree.getTopic(currentEntryKey);
        return null != topicEntries ? topicEntries.size() : 0;
    }

    public static int sizeTopic(EntryKey entryKey) {
        SortedEntryMap topicEntries = entryTree.getTopic(entryKey);
        return null != topicEntries ? topicEntries.size() : 0;
    }

    public static boolean isTopic(EntryKey entryKey) {
        return entryTree.getTopic(entryKey) != null;
    }

    public static void vote(EntryKey entryKey, int delta) {
        Entry entry = getEntry(entryKey);
        if (null == entry) {
            // TODO fix on use swipe with config ..
            logger.warn("vote: entry not found for {}", Entries.toString(entryKey) );
            return;
        }
        // TODO implement vote up logic  -- TreeMap orders by key only
        entry.setRankOffset(delta);
        entryTree.entries.get(entryKey.topic).sortByValue();
        callDataChangedListeners(entryKey, OnDataChanged.ChangeType.ENTRY_RANK_CHANGED);
        // TODO better use 2 functions - set content and set vote
        // or just as virtual entry- attribute with only vote value as content
        String sid = Config.SYSTEM.getValue(); // TODO PERFORMANCE - cache system id !!
        sendEntry( new EntryKey(entryKey.topic, entryKey.nodeId + EntryKey.VOTE_SEPARATOR + sid), entry.getContent() + " | " + entry.myVote, entry);
    }

    public static void sortCurrentTopic() {
        SortedEntryMap topicEntries = entryTree.getTopic(currentEntryKey);
        if (null != topicEntries) {
            topicEntries.sortByValue();
        }
    }


    /*

     */
    public static void logEntries(EntryTree entryTree, String msg) {
        HashMap<Integer, Integer> sizeDistribution = new HashMap<>();
        logger.info("{} - entries dump ({} topics,{} entries):", msg, entryTree.entries.size(), entryTree.size());
        try {
            for (Map.Entry<String, SortedEntryMap> topicEntry : entryTree.entries.entrySet()) {
                String topic = topicEntry.getKey();
                Integer key = topic.length()
                        - topic.replace("/_/","").replace("/", "").length();
                Integer count = sizeDistribution.getOrDefault(key, 0) + key;
                sizeDistribution.put(key,count );
                if (count < 10 || Config.CONFIG_PATH.equals(topic) ) {
                    logger.info(" Topic: '{} ' ({} entries)", topicEntry.getKey(), topicEntry.getValue().size());
                    count = Config.CONFIG_PATH.equals(topic) ? - 1000 : 0 ;
                    for (Map.Entry<String, Entry> nodeEntry : topicEntry.getValue().entrySet()) {
                        logger.info("   Entry: '{}' => '{}'", nodeEntry.getKey(), nodeEntry);
                        if (count++ >= 10 )
                            break;
                    } // for nodeEntry
                } else if (count == 10) {
                    logger.info("   ... (skipp listing entries - at level {})", key);
                }
            } // for topics
        } catch (Exception e) { // java.util.ConcurrentModificationException
            logger.error("logEntries: error logging entries", e);
        }
    }

}
