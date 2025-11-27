package com.example.fayf_android002.Entry;

import java.util.TreeMap;

public class EntryTree {

    public static EntryKey ROOT_ENTRY_KEY = new EntryKey( EntryKey.PATH_SEPARATOR , "");

    public TreeMap<String, TreeMap<String, Entry>> entries = getNewTreeMap();

    public static TreeMap<String,TreeMap<String, Entry>> getNewTreeMap() {
        return new TreeMap<>(new EntryComparator());
    }

    public static TreeMap<String, Entry> getNewTreeChildMap() {
        return new TreeMap<>(new EntryComparator());
    }

    public Entry get(EntryKey key) {
        TreeMap<String, Entry> stringEntryTreeMap = entries.get(key.topic);
        if (null != stringEntryTreeMap) {
            Entry entry = stringEntryTreeMap.get(key.nodeId);
            if (null != entry) {
                return entry;
            }
        }
        return null;
    }

    public TreeMap<String, Entry> getTopic(EntryKey key) {
        TreeMap<String, Entry> stringEntryTreeMap = entries.get(key.topic);
        return stringEntryTreeMap;
    }



    public String remove(EntryKey key) {
        TreeMap<String, Entry> stringEntryTreeMap = entries.get(key.topic);
        if (null != stringEntryTreeMap) {
            Entry entry = stringEntryTreeMap.remove(key.nodeId);
            if (null != entry) {
                return entry.getContent();
            }
        }
        return null;
    }

    public Entry set(EntryKey key, String content) {
        TreeMap<String, Entry> stringEntryTreeMap = entries.computeIfAbsent(key.topic
                , k -> EntryTree.getNewTreeChildMap());
        Entry entry = stringEntryTreeMap.get(key.nodeId);
        if (null == entry) {
            entry = new Entry(content);
            stringEntryTreeMap.put(key.nodeId, entry);
        } else {
            entry.setContent(content);
        }
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
        for (TreeMap<String, Entry> topicEntries : entries.values()) {
            size += topicEntries.size();
        }
        return size;
    }

    public void set(EntryTree entryTree) {
        this.entries = entryTree.entries;
    }
}
