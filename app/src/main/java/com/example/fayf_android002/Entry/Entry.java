package com.example.fayf_android002.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Entry implements java.io.Serializable {

    public static final Logger logger = LoggerFactory.getLogger(Entry.class);

    // Content
    public String content;

    public Entry(String content) {
        this.content = content;
    }

    // Getters and Setters

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
