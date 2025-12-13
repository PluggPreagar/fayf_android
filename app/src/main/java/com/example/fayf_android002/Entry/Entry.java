package com.example.fayf_android002.Entry;

import com.example.fayf_android002.Config;
import com.example.fayf_android002.Util;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Entry implements java.io.Serializable {

    public static final Logger logger = LoggerFactory.getLogger(Entry.class);

    // Content
    public String content;
    public int rank = 0;
    public int myVote = 0; // -1 , 0 , +1
    public int otherVotes = 0;
    public Map<String, Integer> signedVotes = new HashMap<>();

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

    public void setRankOffset( int offset ) {
        this.rank += offset;
        myVote = Integer.compare(rank, 0);
    }

    public void setVote(int voteValue, String voterId) {
        boolean isMyVote = Config.SYSTEM.getValue().equals(voterId); // TODO PERFORMANCE - cache system id !!
        if (isMyVote) {
            if (0 ==this.rank){
                myVote = voteValue;
            } else {
                logger.warn("SKIPP vote for rank. Current rank: {}, new vote: {}", this.rank, voteValue);
                myVote = Integer.compare(this.rank, 0); // keep myVote consistent with rank
            }
        } else {
            if (voterId.isEmpty()) {
                otherVotes = voteValue;
            } else {
                signedVotes.put(voterId, voteValue);
            }
        }
    }

    public int getMyVote() {
        if (this.rank != 0) {
            int myVoteNew = Integer.compare(this.rank, 0);
            if (myVote != myVoteNew) {
                logger.warn("Inconsistent myVote detected. rank: {}, myVote: {}, corrected to: {}"
                        , this.rank, myVote, myVoteNew);
            }
            myVote = myVoteNew;
        }
        return myVote;
    }

    public @NotNull String toString() {
        return "Entry{" +
                "rank=" + rank +
                ", myVote=" + myVote +
                ", other=" + otherVotes +
                ", signed=" + signedVotes +
                ", '" + Util.shortenString( content, 10) + '\'' +
                '}';
    }

}
