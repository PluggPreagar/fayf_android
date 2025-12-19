package com.example.fayf_android002.RuntimeTest;

import android.view.View;

public class ActionQueueEntry {
    public final ACTIONS action;
    public final int fragmentId;
    public int viewId;
    public String text; // for SET_TEXT action
    public long waitTimeMs; // for WAIT action

    public String sourceCodeLine = ""; // for DOC action
    public View view = null; // resolved view for action
    public boolean fixated = false; // whether the view has been found (by text) and fixed

    public enum ACTIONS {
        CLICK,
        LONG_CLICK,
        SET_TEXT,
        IS_TEXT,
        GET_TEXT,
        ASSERT_TEXT,
        IS_VISIBLE,
        WAIT_FOR_VISIBLE,
        DELAY,
        DOC,
        CALL_BACK,
        TEST_BLOCK
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
        this.sourceCodeLine = Thread.currentThread().getStackTrace()[4].toString();
    }
}
