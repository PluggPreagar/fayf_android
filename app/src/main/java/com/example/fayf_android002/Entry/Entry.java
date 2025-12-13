package com.example.fayf_android002.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Entry implements java.io.Serializable {

    public static final Logger logger = LoggerFactory.getLogger(Entry.class);

    // Content
    public String content;
    public int rank = 0;
    public int myVote = 0; // -1 , 0 , +1
    public int otherVotes = 0;

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

    public int getRank() {
        return rank;
    }

    public void setVote(int voteValue, boolean myVote) {
        if (myVote) {
            this.myVote = voteValue;
        } else {
            this.otherVotes += voteValue;
        }
    }

    public void clearVotes() {
        this.myVote = 0;
        this.otherVotes = 0;
    }

}
