package com.example.fayf_android002.Entry;

import com.example.fayf_android002.Config;
import com.example.fayf_android002.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.fayf_android002.Entry.EntryKey.VOTE_SEPARATOR;

public class EntryTree {

    private static final Logger log = LoggerFactory.getLogger(EntryTree.class);
    public static EntryKey ROOT_ENTRY_KEY = new EntryKey( EntryKey.PATH_SEPARATOR , "");

    public SortedEntryTreeMap entries = new SortedEntryTreeMap();


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

    public Entry set(EntryKey key, String content) {
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
                stringEntryTreeMap.put(key.nodeId, entry);
            } else {
                entry.setContent(content);
            } // entry exists

        } // vote ?
        return entry;
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

    public void set(EntryTree entryTree) {
        this.entries = entryTree.entries;
    }
}
