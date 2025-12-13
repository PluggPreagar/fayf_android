package com.example.fayf_android002.Entry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class SortedEntryMap extends LinkedHashMap<String, Entry> {



    public void sortByValue(){
        ArrayList<Entry<String, com.example.fayf_android002.Entry.Entry>> entryList = new ArrayList<>(this.entrySet());
        entryList.sort((e1, e2) -> {
            // rank
            int i = e1.getValue().getRank() - e2.getValue().getRank();
            if (0 == i) {
                // votes
                i = e1.getValue().myVote - e2.getValue().myVote;
                if (0 == i) {
                    i = e1.getValue().getContent().compareTo(e2.getValue().getContent());
                }
            }
            return -i; // descending - most important first
        });
        this.clear();
        for (Entry<String, com.example.fayf_android002.Entry.Entry> entry : entryList) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

}
