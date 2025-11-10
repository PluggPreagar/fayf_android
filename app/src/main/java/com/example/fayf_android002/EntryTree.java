package com.example.fayf_android002;

import kotlinx.serialization.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Serializable
public class EntryTree implements  java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(EntryTree.class);

    protected static final Entry NULL_ENTRY = new Entry(null, null, "");

    /*
        Map of path -> (map of nodeId -> Entry)

        / -> {
            nodeId1 -> Entry1,
            nodeId2 -> EntryPath2       // 2. Entry is path for another set of entries
        }
        /nodeId2 -> {
            nodeId2.1 -> Entry2.1
        }

    */
    public TreeMap<String, TreeMap<String, Entry>> entries = new TreeMap<>();

    public Entry getEntry(String topic, String nodeId) {
        TreeMap<String, Entry> entryTreeMap = entries.get(topic);
        return null == entryTreeMap ? NULL_ENTRY : entryTreeMap.getOrDefault(nodeId, NULL_ENTRY);
    }

    public Entry getEntry(String fullPath) {
        // split on last "/"
        int lastSlashIndex = fullPath.lastIndexOf("/");
        if (lastSlashIndex == -1 || (1 != fullPath.length() && lastSlashIndex == fullPath.length() - 1)) {
            //  only "/" , "/parent/" are valid
            //  "", "node", "/node/", "parent/node/" are invalid
            logger.warn("Invalid fullPath format: {}", fullPath);
            return NULL_ENTRY;
        }
        // "/"  -> topic = "/" , nodeId = ""
        // "/node"  -> topic = "/" , nodeId = "node"
        // "/parent/node"  -> topic = "/parent" , nodeId = "node"
        String topic = 0 == lastSlashIndex ? "/" : fullPath.substring(0, lastSlashIndex);
        String nodeId = fullPath.substring(lastSlashIndex + 1);
        return getEntry(topic, nodeId);
    }


    public List<EntryTree> getEntries(String topic, int offset, int limit) {
        List<EntryTree> entries_found = new ArrayList<>();
        if (entries.containsKey(topic)) {
            TreeMap<String, Entry> topicEntries = entries.getOrDefault(topic, new TreeMap<>());
            for (Map.Entry<String, Entry> e : topicEntries.entrySet()) {
                if (offset > 0 ) {
                    offset--;
                } else if (limit > 0) {
                    //logger.info("Entry: {}", e.getValue().content);
                    entries_found.add(this);
                    limit--;
                } else {
                    break;
                }
            }
        } else {
            logger.warn("No entries found for topic: \"{}\"", topic);
        }
        return entries_found;
    }


    public Iterator<Map.Entry<String, Entry>> getEntriesIterator(String topic, int offset) {
        Iterator<Map.Entry<String, Entry>> iterator = Collections.emptyIterator();

        if (entries.containsKey(topic)) {
            TreeMap<String, Entry> topicEntries = entries.getOrDefault(topic, new TreeMap<>());
            Iterator<Map.Entry<String, Entry>> i = topicEntries.entrySet().iterator();
            // advance to offset
            while (i.hasNext() && offset > 0) {
                i.next();
                offset--;
            }
            if (i.hasNext()) {
                iterator = i;
            } else {
                logger.warn("No entries found at offset {} for topic: {}", offset, topic);
            }
        } else {
            logger.warn("No entries found for topic: {}", topic);
        }
        return iterator;
    }






    public Entry addEntry(String path, Entry entry) {
        entries.putIfAbsent(path, new TreeMap<>(new EntryComparator()));
        entries.get(path).put(entry.nodeId, entry);
        return entry;
    }

    public void removeEntry(Entry entry) {
        String path = entry.getTopic();
        if (entries.containsKey(path)) {
            TreeMap<String, Entry> topicEntries = entries.get(path);
            if (topicEntries.containsKey(entry.nodeId)) {
                topicEntries.remove(entry.nodeId);
                logger.info("Entry removed: {}", entry.getFullPath());
            } else {
                logger.warn("Entry nodeId not found for removal: {}", entry.getFullPath());
            }
        } else {
            logger.warn("Entry path not found for removal: {}", entry.getFullPath());
        }
    }

    /*

        COMPARATOR

     */

    private static class EntryComparator implements java.util.Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

    /*
      HELPER
     */

    public int size() {
        int size = 0;
        for (Map.Entry<String, TreeMap<String, Entry>> e : entries.entrySet()) {
            size += e.getValue().size();
        }
        return size;
    }

}
