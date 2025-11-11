package com.example.fayf_android002;

import java.util.Comparator;

class EntryComparator implements Comparator<String>, java.io.Serializable {


    @Override
    public int compare(String o1, String o2) {
        return o1.compareTo(o2);
    }
}
