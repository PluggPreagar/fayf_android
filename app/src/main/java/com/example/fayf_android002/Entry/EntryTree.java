package com.example.fayf_android002.Entry;

public class EntryTree {

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
        SortedEntryMap stringEntryTreeMap = entries.computeIfAbsent(key.topic
                , k -> new SortedEntryMap());
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
        for (SortedEntryMap topicEntries : entries.values()) {
            size += topicEntries.size();
        }
        return size;
    }

    public void set(EntryTree entryTree) {
        this.entries = entryTree.entries;
    }
}
