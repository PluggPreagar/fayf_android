package com.example.fayf_android002.Entry;

import junit.framework.TestCase;
import org.junit.Test;

public class EntryKeyTest extends TestCase {

    public void entryKeyCreation(String key, String topic , String nodeId) {
        EntryKey entryKey = new EntryKey(key);
        assertEquals(topic, entryKey.topic, key + " -SHOULD-BE-> " + topic + " : " + entryKey.topic);
        assertEquals(nodeId, entryKey.nodeId, key + " -SHOULD-BE-> " + nodeId + " : " + entryKey.nodeId);
    }

    @Test
    public void testEntryKeyCreation() {
        entryKeyCreation("", "/", "");
        entryKeyCreation("/leadingSlashNodeId", "/leadingSlashNodeId", "");
        entryKeyCreation("trailingSlashNodeId/", "/trailingSlashNodeId", "");
        entryKeyCreation("singleNodeId", "", "/singleNodeId");
        entryKeyCreation("topic/nodeId", "/topic", "nodeId");
        entryKeyCreation("topic/subtopic/nodeId", "/topic/subtopic", "nodeId");
        entryKeyCreation("/topic/subtopic/nodeId/", "/topic/subtopic", "nodeId");
        entryKeyCreation("/topic/subtopic/nodeId", "/topic/subtopic", "nodeId");
    }


}