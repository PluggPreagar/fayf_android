package com.example.fayf_android002.RuntimeTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class ActionQueue {

    Logger logger = LoggerFactory.getLogger(ActionQueue.class);

    List<ActionQueueEntry> actionQueue = new LinkedList<>();

    int fragmentId = -1;
    int viewId = -1;

    int defaultDelayMs = 100;

    String currentText = null;


    public ActionQueue(int FragmentId) {
        this.fragmentId = FragmentId;
    }


    public ActionQueue addAction(ActionQueueEntry action) {
        actionQueue.add(action);
        if (action.fragmentId != -1) {
            this.fragmentId = action.fragmentId;
        }
        if (action.viewId != -1) {
            this.viewId = action.viewId;
        }
        return this;
    }


    public void run () {
        logger.info("Starting ActionQueue with {} actions", actionQueue.size());
        // wait
        new Thread(() -> {
            while(run_next()) {
                // continue
            }
            if (!actionQueue.isEmpty()) {
                logger.warn("ActionQueue ended but still has {} actions remaining", actionQueue.size());
            }
            logger.info("ActionQueue {} completed", this);
        }).start();
    }


    private boolean run_next() {
        ActionExecutor actionExecutor = new ActionExecutor();
        if (!actionQueue.isEmpty()) {
            ActionQueueEntry action = actionQueue.remove(0);
            actionExecutor.executeAction(action, this);
            // wait a bit between actions
            try {
                Thread.sleep(defaultDelayMs);
            } catch (InterruptedException e) {
                logger.error("Interrupted during action delay", e);
            }
        }
        return true;
    }

    /*
        I N I T
     */

    public ActionQueue setFragment(int fragmentId) {
        this.fragmentId = fragmentId;
        return this;
    }

    public ActionQueue setDefaultDelayMs(int delayMs) {
        this.defaultDelayMs = delayMs;
        return this;
    }


    /*
        A C T I O N S
     */

    public ActionQueue click( int viewId) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.CLICK
                , fragmentId, viewId, null, 0, null));
        return this;
    }

    public ActionQueue longClick( int viewId) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.LONG_CLICK
                , fragmentId, viewId, null, 0, null));
        return this;
    }

    public ActionQueue setText( int viewId, String text) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.SET_TEXT
                , fragmentId, viewId, text, 0, null));
        return this;
    }

    public ActionQueue getText( int viewId) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.GET_TEXT
                , fragmentId, viewId, null, 0, null));
        return this;
    }

    public ActionQueue assertText( int viewId, String expectedText) {
        // short for getText + assertText
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.ASSERT_TEXT
                , fragmentId, viewId, expectedText, 0, null));
        return this;
    }

    public ActionQueue assertText(String expectedText) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.ASSERT_TEXT
                , -1, -1, expectedText, 0, null));
        return this;
    }

    public ActionQueue isVisible( int viewId) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.IS_VISIBLE
                , fragmentId, viewId, null, 0, null));
        return this;
    }

    public ActionQueue waitForVisible( int viewId) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.WAIT_FOR_VISIBLE
                , fragmentId, viewId, null, 0, null));
        return this;
    }

    public ActionQueue delay(long waitTimeMs) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.DELAY
                , fragmentId, -1, null, waitTimeMs, null));
        return this;
    }

    public ActionQueue callBack(ActionCallback callback) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.CALL_BACK
                , fragmentId, -1, null, 0, callback));
        return this;
    }

    public void setLastRetrievedText(String text) {
        this.currentText = text;
    }

    public String getLastRetrievedText() {
        return currentText;
    }
}
