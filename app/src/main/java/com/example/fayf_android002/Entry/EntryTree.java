package com.example.fayf_android002.Entry;

import com.example.fayf_android002.Config;
import com.example.fayf_android002.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static com.example.fayf_android002.Entry.EntryKey.VOTE_SEPARATOR;

public class EntryTree implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(EntryTree.class);
    public static EntryKey ROOT_ENTRY_KEY = new EntryKey( EntryKey.PATH_SEPARATOR , "");

    public SortedEntryTreeMap entries = new SortedEntryTreeMap();

    private static boolean isSortingInvalid = false;



    public Entry get(EntryKey key) {
        SortedEntryMap stringEntryTreeMap = entries.get(key.topic);
        if (null != stringEntryTreeMap) {
            Entry entry = stringEntryTreeMap.get(key.nodeId);
            if (null != entry) {
                return entry;
            }
        }
        return null;
    }

    public SortedEntryMap getTopic(EntryKey key) {
        return entries.get(key.getFullPath());
    }



    public String remove(EntryKey key) {
        SortedEntryMap stringEntryTreeMap = entries.get(key.topic);
        if (null != stringEntryTreeMap) {
            Entry entry = stringEntryTreeMap.remove(key.nodeId);
            if (null != entry) {
                return entry.getContent();
            }
        }
        return null;
    }

    public Entry load(EntryKey key, String content) {
        return setPublic(key, content, false);
    }

    public Entry setPublic(EntryKey key, String content) {
        return setPublic(key, content, true);
    }


    public Entry setPublic(EntryKey key, String content, boolean initRank) {
        // entry: EntryKey(<topic>, <nodeId>)                     content: <content>
        // vote:  EntryKey(<topic>, <nodeId>"::Vote::"<voterId>)  content: <content>" | "<voteValue>
        SortedEntryMap stringEntryTreeMap = entries.computeIfAbsent(key.topic
                , k -> new SortedEntryMap());
        // TODO - check if all attributes go into separate Entries (or to be stored in Entry as Fields)
        Entry entry = null;
        if (key.nodeId.contains(VOTE_SEPARATOR)) {

            String[] splitId = key.nodeId.split(VOTE_SEPARATOR); // ::Vote::[voterId]
            // split last occurrence only - use regex with negative lookahead
            String[] splitContent = content.split(" \\| (?=[^|]+$)"); // content | voteValue
            if (splitId.length != 2 && splitId.length != 1) {
                log.error("Invalid vote nodeId: {}", key.nodeId);
            } else if (splitContent.length != 2) {
                log.error("Invalid vote content: {}", content);
            } else {
                // valid
                entry = stringEntryTreeMap.get(splitId[0]); // ohne vote suffix
                if (null != entry) {
                    String voterId = splitId.length < 2 ? "" : splitId[1]; // me or others
                    int voteValue = Util.parseIntOr(splitContent[1], 0);
                    entry.setVote(voteValue, voterId);
                } // entry exists
            } // valid split

        } else {

            entry = stringEntryTreeMap.get(key.nodeId);
            if (null == entry) {
                entry = new Entry(content);
                if (initRank) {
                    entry.setRankOffset(1); // new entry starts with rank 1 - should be of interest
                }
                stringEntryTreeMap.put(key.nodeId, entry);
            } else {
                entry.setContent(content);
            } // entry exists

        } // vote ?
        isSortingInvalid = false; // do not sort on every set, but on demand
        return entry;
    }

    public static void markSortingInvalid() {
        isSortingInvalid = true;
    }

    public static boolean isSortingInvalid() {
        return isSortingInvalid;
    }

    public static void markSortingValid() {
        isSortingInvalid = false;
    }



    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public static boolean isRootKey(EntryKey key) {
        return ROOT_ENTRY_KEY.topic.equals(key.topic) && ROOT_ENTRY_KEY.nodeId.equals(key.nodeId);
    }



    public int size() {
        int size = 0;
        for (SortedEntryMap topicEntries : entries.values()) {
            size += topicEntries.size();
        }
        return size;
    }

    /*
         MERGE REMOVE ...
     */


    public static EntryTree filter(EntryTree entryTree, boolean hiddenPart) {
        Set<String> keys = new HashSet<>(entryTree.entries.keySet());
        keys.removeIf(t ->  (t.startsWith(Config.CONFIG_PATH) || t.startsWith("/_/")) != hiddenPart );
        keys.forEach( topic -> entryTree.entries.remove(topic) );
        return entryTree;
    }

    public static EntryTree filterConfig(EntryTree entryTree){
        return filter(entryTree, true);
    }

    public static EntryTree filterNonConfig(EntryTree entryTree){
        return filter(entryTree, false);
    }


    public void setPublic(EntryTree entryTree) {
        // remove hidden entries / config - must be set separately (from Config or local storage)
        // clone to avoid ConcurrentModificationException
        filterNonConfig(entryTree);
        // remove all public entries and move over new ones
        filterConfig(this);
        merge(this, entryTree);
        this.entries = entryTree.entries;
    }

    public void setPrivate(EntryTree entryTree) {
        // remove all hidden entries / config - must be set separately (from Config or local storage)
        filterConfig(entryTree);
        // remove all private entries and move over new ones
        filterNonConfig(this);
        merge(this, entryTree);
        this.entries = entryTree.entries;
    }



    public static void merge(EntryTree target, EntryTree source) {
        source.entries.forEach( (topic, entryMap) -> {
            SortedEntryMap targetEntryMap = target.entries.get(topic);
            if (null == targetEntryMap) {
                targetEntryMap = new SortedEntryMap();
                target.entries.put(topic, targetEntryMap);
            } else {
                SortedEntryMap finalTargetEntryMap = targetEntryMap;
                entryMap.forEach( (nodeId, entry) -> finalTargetEntryMap.put(nodeId, entry) );
            }
        });
    }


}
