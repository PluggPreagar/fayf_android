package com.example.fayf_android002.Entry;

import com.example.fayf_android002.Config;
import com.example.fayf_android002.RuntimeTest.RuntimeChecker;
import com.example.fayf_android002.Util;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Entry implements java.io.Serializable {

    public static final Logger logger = LoggerFactory.getLogger(Entry.class);

    // Content
    private String content;
    public int rank = 0;
    public int myVote = 0; // -1 , 0 , +1
    public int otherVotes = 0;

    public long userLastUpdateTime = 0L;
    public long otherLastUpdateTime = 0L;

    public Map<String, Integer> signedVotes = new HashMap<>();

    public Entry(String content) {
        this.content = content;
    }

    private void entryModified(boolean byUser) {
        if (byUser) {
            userLastUpdateTime = System.currentTimeMillis();
        } else {
            otherLastUpdateTime = System.currentTimeMillis();
        }
    }

    // Getters and Setters

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        Entries.entryModified();
        this.content = content;
    }

    public int getRank() {
        return rank;
    }

    public void setRankOffset( int offset ) {
        entryModified(true);
        this.rank += offset;
        myVote = Integer.compare(rank, 0);
        EntryTree.markSortingInvalid();
        RuntimeChecker.check();
    }

    public void setVote(int voteValue, String voterId) {
        // only used by loading !!
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
        EntryTree.markSortingInvalid();
        RuntimeChecker.check();
    }


    public void merge(Entry previous) {
        // on load - keep private settings
        if (previous.userLastUpdateTime > 0 && this.userLastUpdateTime > 0) {
            if (previous.userLastUpdateTime > this.userLastUpdateTime) {
                this.content = previous.content;
                this.userLastUpdateTime = previous.userLastUpdateTime;
            }
        } else {
            if (!previous.content.isEmpty()) {
                this.content = previous.content;
            }
            if (previous.rank != 0 || previous.myVote != 0) {
                this.rank = previous.rank;
                this.myVote = previous.myVote;
            }
        }
        if (previous.otherLastUpdateTime > 0 && this.otherLastUpdateTime > 0) {
            if (previous.otherLastUpdateTime > this.otherLastUpdateTime) {
                this.otherVotes = previous.otherVotes;
                this.signedVotes = previous.signedVotes;
                this.otherLastUpdateTime = previous.otherLastUpdateTime;
            }
        } else {
            this.otherVotes = previous.otherVotes != 0 ? previous.otherVotes : this.otherVotes;
            this.signedVotes = !previous.signedVotes.isEmpty() ? previous.signedVotes : this.signedVotes;
        }
        RuntimeChecker.check();
        EntryTree.markSortingInvalid();
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
