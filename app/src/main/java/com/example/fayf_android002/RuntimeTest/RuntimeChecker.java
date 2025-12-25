package com.example.fayf_android002.RuntimeTest;

import com.example.fayf_android002.Config;
import com.example.fayf_android002.Entry.Entries;
import com.example.fayf_android002.Entry.Entry;
import com.example.fayf_android002.Entry.EntryKey;
import com.example.fayf_android002.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeChecker {

    private static final Logger log = LoggerFactory.getLogger(RuntimeChecker.class);

    public static boolean isStatus = false;
    private static String callStack = "";

    public static boolean checkStatus() {
        boolean status = false;
        EntryKey entryKey = new EntryKey(Config.CONFIG_PATH, "tenant");
        Entry entry = Entries.getEntry(entryKey);
        if (entry == null) {
            log.warn("RuntimeChecker.checkStatus: tenant entry is null");
        } else if (entry.getRank() < 1) {
            log.debug("RuntimeChecker.checkStatus: tenant entry rank is " + entry.getRank());
        } else if (!Entries.isTopic(entryKey)) {
            log.debug("RuntimeChecker.checkStatus: tenant entry is not a topic");
        } else {
            status = true;
        }
        return status;
    }

    public static void check() {
        // This method is intentionally left blank.
        // It serves as a placeholder for runtime checks.
        boolean curStatus = checkStatus();
        String curCallStack = UtilDebug.getCompactCallStack("RuntimeChecker.check: " + curStatus);
        // remove first 2 lines
        if (callStack.isEmpty()){
            log.warn("RuntimeChecker: init to " + curStatus);
            log.info("Call Stack:\n" + curCallStack);
        } else if (curStatus != isStatus) {
            log.warn("RuntimeChecker: isStatus changed to " + curStatus);
            log.info("changed after:\n" + callStack);
            log.info("Call Stack:\n" + curCallStack);
        } else if (!curStatus) {
            log.warn("RuntimeChecker: " + curStatus );
        } else {
            log.debug("RuntimeChecker: " + curStatus );
        }
        callStack = curCallStack;
        isStatus = curStatus;
    }

}
