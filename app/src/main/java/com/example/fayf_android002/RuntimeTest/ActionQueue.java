package com.example.fayf_android002.RuntimeTest;

import android.widget.Toast;
import com.example.fayf_android002.Entry.Entries;
import com.example.fayf_android002.MainActivity;
import com.example.fayf_android002.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ActionQueue {

    private static final boolean CONTINUE_ON_ERROR = false;
    Logger logger = LoggerFactory.getLogger(ActionQueue.class);

    List<ActionQueueEntry> actionQueue = new LinkedList<>();

    int fragmentId = -1;
    int viewId = -1;

    int defaultDelayMs = 100;

    public final static int ID_BACK = -1001;
    public final static int ID_UP = -1002;

    String currentText = null;

    private final String caller;
    public static int TO_BE_FOUND = -9999;

    public ActionQueue(int FragmentId) {
        this.fragmentId = FragmentId;
        // get calling method for logging
        this.caller = getCaller();
    }

    private String getCaller() {
        String caller = "";
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // search first method package fayf_android002
        for (int i = 2; i < stackTrace.length ; i++) {
            StackTraceElement callingMethod = stackTrace[i];
            if (callingMethod.getClassName().contains("com.example.fayf_android002.RuntimeTest")
                    && !callingMethod.getClassName().contains(".ActionQueue")) {
                caller = callingMethod.getClassName() + "." + callingMethod.getMethodName() + "():"
                        + callingMethod.getLineNumber();
                logger.info("ActionQueue created by method: {}", caller );
                break;
            }
        }
        return caller;
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
        int initialSize = actionQueue.size();
        AtomicInteger skipped = new AtomicInteger();
        logger.info("Starting ActionQueue with {} actions", actionQueue.size());
        // wait
        new Thread(() -> {
            ActionExecutor actionExecutor = new ActionExecutor();
            // check reset - Test_Block, make sure app is in expected state / give time to settle
            actionQueue.add(0, new ActionQueueEntry(ActionQueueEntry.ACTIONS.TEST_BLOCK
                    , fragmentId, -1, "init check", 0, null));
            //
            while (!actionQueue.isEmpty()) {
                while (run_next(actionExecutor)) {
                    // continue
                }
                // if exit due to error, forward to next text block and try to resume
                if (!actionQueue.isEmpty()) {
                    if (CONTINUE_ON_ERROR) {
                        logger.info("ActionQueue attempting to resume after error");
                    } else {
                        break; // exit
                    }
                    // set to root topic
                    Entries.rootTopic();
                    while (!actionQueue.isEmpty()
                            && actionQueue.get(0).action != ActionQueueEntry.ACTIONS.TEST_BLOCK) {
                        skipped.getAndIncrement();
                        actionQueue.remove(0);
                    } // forward to next test block
                } // error
            } // resume until empty
            if (!actionExecutor.errorMsg.isEmpty()) {
                //logger.error("{}", caller);
                logger.error("ActionQueue error: {}", actionExecutor.errorMsg.get(0));
            }
            if (!actionQueue.isEmpty()) {
                logger.warn("ActionQueue ended but still has {} actions remaining", actionQueue.size());
                if (!actionExecutor.errorMsg.isEmpty()) {
                    MainActivity.getInstance().runOnUiThread(() -> {
                        Toast.makeText(MainActivity.getInstance().getApplicationContext()
                                , "ActionQueue error: " + actionExecutor.errorMsg.get(0)
                                        + "\n " + caller
                                , Toast.LENGTH_SHORT).show();
                            });
                }
            }
            String msg  = ( actionExecutor.errorMsg.isEmpty() ? " PASSED \n ✅ TEST PASSED\n"
                            : " FAIL \n  ❌ TEST FAILED (" + actionExecutor.errorMsg.size() + ")\n") +
                    "===========================\n" +
                    "ActionQueue summary: executed {} actions, {} errors, {} skipped of {} total\n" +
                    "ActionQueue {} completed\n" +
                    "===========================" ;
            if (actionExecutor.errorMsg.isEmpty()) {
                logger.info(msg
                    , initialSize - actionQueue.size() - actionExecutor.errorMsg.size() - skipped.get()
                    , actionExecutor.errorMsg.size()
                    , actionQueue.size() + skipped.get()
                    , initialSize
                    , caller
                );
            } else {
                logger.error(msg
                        , initialSize - actionQueue.size() - actionExecutor.errorMsg.size() - skipped.get()
                        , actionExecutor.errorMsg.size()
                        , actionQueue.size() + skipped.get()
                        , initialSize
                        , this
                );
            }
            MainActivity.userInfo("ActionQueue " + (actionExecutor.errorMsg.isEmpty() ? "PASSED" : "FAILED")
                    + ": executed " + (initialSize - actionQueue.size() - actionExecutor.errorMsg.size() - skipped.get())
                    + " actions, " + actionExecutor.errorMsg.size() + " errors, "
                    + (actionQueue.size() + skipped.get()) + " skipped of " + initialSize + " total"
            );
            logger.info("ActionQueue ended 4");
        }).start();
    }

    private boolean run_next(ActionExecutor actionExecutor) {
        if (!actionQueue.isEmpty()) {
            // wait a bit between actions
            try {
                Thread.sleep(defaultDelayMs);
            } catch (InterruptedException e) {
                logger.error("Interrupted during action delay", e);
            }
            ActionQueueEntry action = actionQueue.remove(0);
            actionExecutor.executeAction(action, this);
        }
        return actionExecutor.errorMsg.isEmpty() && !actionQueue.isEmpty();
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

    public ActionQueue click( int viewId, int delayMs) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.CLICK
                , fragmentId, viewId, null, delayMs, null));
        return this;
    }

    public ActionQueue click( int viewId) {
        return click(viewId, 0);
    }

    public ActionQueue click(String text) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.CLICK
                , fragmentId, TO_BE_FOUND, text, 0, null));
        return this;
    }

    public ActionQueue longClick( int viewId, int delayMs) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.LONG_CLICK
                , fragmentId, viewId, null, delayMs, null));
        return this;
    }

    public ActionQueue longClick( int viewId) {
        return longClick(viewId, 0);
    }

    public ActionQueue longClick(String text) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.LONG_CLICK
                , fragmentId, TO_BE_FOUND, text, 0, null));
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

    public ActionQueue isText( int viewId, String expectedText) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.IS_TEXT
                , fragmentId, viewId, expectedText, 0, null));
        return this;
    }

    public ActionQueue isVisible( int viewId) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.IS_VISIBLE
                , fragmentId, viewId, null, 0, null));
        return this;
    }

    public ActionQueue isVisible(int viewId, String s) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.IS_VISIBLE
                , fragmentId, viewId, s, 0, null));
        return this;
    }

    public ActionQueue hasTitle(String title) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.IS_VISIBLE
                , fragmentId, TO_BE_FOUND , title, 0, null));
        return this;
    }


    public ActionQueue waitForVisible( int viewId) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.WAIT_FOR_VISIBLE
                , fragmentId, viewId, null, 0, null));
        return this;
    }

    public ActionQueue waitForVisible( int viewId, String text) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.WAIT_FOR_VISIBLE
                , fragmentId, viewId, text, 0, null));
        return this;
    }

    public ActionQueue waitForVisible( String text) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.WAIT_FOR_VISIBLE
                , fragmentId, TO_BE_FOUND , text, 0, null));
        return this;
    }

    public ActionQueue waitForVisible( String text, long timeoutMs) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.WAIT_FOR_VISIBLE
                , fragmentId, TO_BE_FOUND , text, timeoutMs, null));
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

    public ActionQueue doc(String msg) {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.DOC
                , fragmentId, -1, msg, 0, null));
        return this;
    }

    public ActionQueue testBlock(String msg) {
        // check current state, let app settle before
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.DELAY
                , fragmentId, -1, "", 2000, null));
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.WAIT_FOR_VISIBLE
                , fragmentId, -1 , "c1", 2000, null));
        // wait a bit
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.DELAY
                , fragmentId, -1, "", 200, null));
        // mark a test block - reset checks
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.TEST_BLOCK
                , fragmentId, -1, msg, 0, null));
        return this;
    }


    public ActionQueue clickUp() {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.CLICK
                , fragmentId, ID_UP, "UP", 0, null));
        return this;
    }

    public ActionQueue clickBack() {
        addAction(new ActionQueueEntry(ActionQueueEntry.ACTIONS.CLICK
                , fragmentId, ID_BACK, "BACK", 0, null));
        return this;
    }

}
