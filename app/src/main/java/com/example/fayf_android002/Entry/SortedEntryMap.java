package com.example.fayf_android002.Entry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class SortedEntryMap extends LinkedHashMap<String, Entry> {



    public void sortByValue(){
        ArrayList<Entry<String, com.example.fayf_android002.Entry.Entry>> entryList = new ArrayList<>(this.entrySet());
        entryList.sort((e1, e2) -> {
            int i = e1.getValue().getRank() - e2.getValue().getRank();
            if (i != 0) {
                return -i;
            }
            return e1.getValue().getContent().compareTo(e2.getValue().getContent());
        });
        Map<String, com.example.fayf_android002.Entry.Entry> sortedMap = new LinkedHashMap<>();
        for (Entry<String, com.example.fayf_android002.Entry.Entry> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        this.clear();
        this.putAll(sortedMap);
    }

}
