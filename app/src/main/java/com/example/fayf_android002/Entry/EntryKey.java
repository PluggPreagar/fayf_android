package com.example.fayf_android002.Entry;

import androidx.annotation.NonNull;
import org.jetbrains.annotations.NotNull;

public class EntryKey {

    public static final String PATH_SEPARATOR = "/";
    public String topic;
    public String nodeId;

    public EntryKey(String topic, String nodeId) {
        this.topic = sanitizeTopic(topic); // ensure "" -> "/"
        this.nodeId = nodeId;
    }

    public EntryKey(String fullPath) {
        // split on last "/"
        // valid formats:    "/" , "/node" , "/parent/node" , "/parent/subparent/node"
        // fix invalid formats: "" -> "/" , "node" -> "/node" , "parent/node/" -> "/parent/node"
        fullPath = sanitizeTopic(fullPath);
        int lastSlashIndex = fullPath.lastIndexOf("/");
        topic = lastSlashIndex == 0 ? PATH_SEPARATOR : fullPath.substring(0, lastSlashIndex); // up to last "/" , at least "/"
        nodeId = fullPath.substring( lastSlashIndex + 1); // after last "/"
    }

    public static String sanitizeTopic(String topic) {
        // allow "/" , "/parent", "/parent/node" etc. - remove trailing slash if any
        if (topic == null || topic.isEmpty()) {
            topic = "/";
        }else {
            if (!topic.startsWith("/")) {
                topic = "/" + topic;
            } else if (topic.startsWith("//")) {
                topic = topic.substring(1);
            }
            if (topic.length() > 1 && topic.endsWith("/")) {
                topic = topic.substring(0, topic.length() - 1);
            }
        }
        return topic;
    }


    public String getFullPath() {
        return ( PATH_SEPARATOR.equals(topic) ? topic : topic + PATH_SEPARATOR ) + nodeId;
    }

    @NonNull
    @Override
    public @NotNull java.lang.String toString() {
        return getFullPath();
    }
}
