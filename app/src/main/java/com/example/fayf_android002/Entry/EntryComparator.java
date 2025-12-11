package com.example.fayf_android002.Entry;

import java.util.Comparator;
import java.util.Map;

class EntryComparator implements Comparator<String>, java.io.Serializable {

    @Override
    public int compare(String o1, String o2) {
        return o1.compareTo(o2);
    }
}
