package com.example.fayf_android002.RuntimeTest;

public class ActionQueueEntry {
    public final ACTIONS action;
    public final int fragmentId;
    public final int viewId;
    public final String text; // for SET_TEXT action
    public final long waitTimeMs; // for WAIT action

    public enum ACTIONS {
        CLICK,
        LONG_CLICK,
        SET_TEXT,
        GET_TEXT,
        ASSERT_TEXT,
        IS_VISIBLE,
        WAIT_FOR_VISIBLE,
        DELAY,
        CALL_BACK
    }


    public ActionCallback callback = null; // for CALL_BACK action

    public ActionQueueEntry(ACTIONS action, int fragmentId, int viewId, String text
            , long waitTimeMs, ActionCallback callback) {
        this.action = action;
        this.fragmentId = fragmentId;
        this.viewId = viewId;
        this.text = text;
        this.waitTimeMs = waitTimeMs;
        this.callback = callback;
    }
}
