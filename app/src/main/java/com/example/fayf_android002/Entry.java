package com.example.fayf_android002;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class Entry implements java.io.Serializable {

    public static final Logger logger = LoggerFactory.getLogger(Entry.class);

    protected static final String DELETION_SUFFIX = "--";

    protected static final String TENANT_ENTRY_PATH = "/_/tenants";

    protected static final String HIDDEN_ENTRY_PATH = "/_";

    protected static final String PATH_SEPARATOR = "/";

    public String topic = ""; // might be empty
    public String nodeId;
    public String content;

    public HashMap<String, String> attributes = new HashMap<>();

    public HashMap<String, String> metadata = new HashMap<>();

    public Entry(String topic, String nodeId, String content) {
        if (topic == null || topic.isEmpty()) {
            topic = "/";
        }
        this.topic = topic;
        this.nodeId = nodeId;
        this.content = content;
    }

    public Entry(String fullPath, String content) {
        this.topic = getTopicFromFullPath(fullPath);
        this.nodeId = getNodeIdFromFullPath(fullPath);
        this.content = content;
    }



    // allow String to be invalid
    public static Entry build(String entryString) {
        Entry entry = null;
        // 2025-10-31 14:09:49,/ | 1761916190 | heute
        String[] rawParts = entryString.split(",", 2);
        String[] parts = rawParts.length > 1 ? rawParts[1].split(" \\| ", 3) : new String[]{};
        if (!rawParts[0].isEmpty() && parts.length > 2 && parts.length < 5 ) {
            entry = new Entry(parts[0].trim(), parts[1].trim(), parts[2].trim());
            entry.attributes.put("timestamp", rawParts[0].trim());
            logger.info("Built Entry from string: {}", entryString);
        }  else {
            logger.warn("Failed to build Entry from string: {}", entryString);
        }
        //
        return entry;
    }

    public String buildStringRepresentation() {
        return topic + " | " + nodeId + " | " + content;
    }


    /* FULL PATH

     */

    public String getFullPath() {
        String path = this.topic;
        if (path == null || path.isEmpty()) {
            path ="/";
        }
        path = path +  (path.endsWith("/") ? "" : "/") + this.nodeId;
        if (path.contains("//")) {
            path = path.replaceAll("//+", "/");
        }
        return path;
    }


    public boolean isRootTopic() {
        return this.getFullPath().equals("/");
    }


    /*

        GETTERS

     */

    public String getTopic() {
        return this.topic;
    }

    public String getNodeId() {
        return this.nodeId;
    }

    public String getContent() {
        return this.content;
    }


    /*
        ID Manipulation Helper
     */

    public static String getNodeIdFromFullPath(String fullPath) {
        int lastSlashIndex = fullPath.lastIndexOf("/");
        return fixNodeAndWarn(lastSlashIndex <=1  ? "/" : fullPath.substring(lastSlashIndex + 1));
    }

    public static String getTopicFromFullPath(String fullPath) {
        int lastSlashIndex = fullPath.lastIndexOf("/");
        return fixTopicAndWarn(  lastSlashIndex <=1  ? "/" : fullPath.substring(0, lastSlashIndex));
    }

    public static String getParent(String fullPath) {
        int lastSlashIndex = fullPath.lastIndexOf("/");
        return fixTopicAndWarn( lastSlashIndex <=1  ? "/" : fullPath.substring(0, lastSlashIndex));
    }



    private static String fixTopicAndWarn(String topic) {
        String fixedTopic = fixTopic(topic);
        if (!fixedTopic.equals(topic)) {
            logger.warn("Fixed topic from \"{}\" to \"{}\"", topic, fixedTopic);
        }
        return fixedTopic;
    }

    public static String fixTopic(String topic) {
        // allow "/" , "/parent", "/parent/node" etc. - remove trailing slash if any
        if (topic == null || topic.isEmpty()) {
            topic = "/";
        }else {
            if (!topic.startsWith("/")) {
                topic = "/" + topic;
            }
            if (topic.length() > 1 && topic.endsWith("/")) {
                topic = topic.substring(0, topic.length() - 1);
            }
        }
        return topic;
    }

    public static String fixNodeAndWarn(String nodeId) {
        String fixedNodeId = fixNode(nodeId);
        if (!fixedNodeId.equals(nodeId)) {
            logger.warn("Fixed nodeId from \"{}\" to \"{}\"", nodeId, fixedNodeId);
        }
        return fixedNodeId;
    }

    public static String fixNode(String nodeId) {
        if (nodeId == null) {
            nodeId = "";
        } // allow node "asasd" and absolute paths "/parent/node"
        return nodeId;
    }


    /*
        SETTERS
     */

    public void setContent(String value) {
        this.content = value;
    }
}
