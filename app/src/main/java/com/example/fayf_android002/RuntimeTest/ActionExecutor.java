package com.example.fayf_android002.RuntimeTest;

import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.Button;
import androidx.fragment.app.FragmentActivity;
import com.example.fayf_android002.Entry.Entries;
import com.example.fayf_android002.Entry.EntryTree;
import com.example.fayf_android002.MainActivity;
import com.example.fayf_android002.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.view.View;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class ActionExecutor {

    public List<String> errorMsg = new ArrayList<>();
    Logger logger = LoggerFactory.getLogger(ActionExecutor.class);
    private String testContext = "";
    private ActionQueueEntry currentAction = null;

    private String logName(View view) {
        return "\""+ UtilDebug.getResourceName(view) + "\" (" + view.getClass().getSimpleName() + " " + view.getId() + " )";
    }

    public void executeAction(ActionQueueEntry action, ActionQueue actionQueue) {
        // switch on action type
        currentAction = action;
        Object view = getViewOptional(action);
        if ( view == null ) {
            view = MainActivity.getInstance().menu.findItem(action.viewId); // ensure menu is initialized
        }
        if ( ActionQueueEntry.ACTIONS.TEST_BLOCK == action.action ) {
            logger.info("---------------------------------------");
        } else if ( ActionQueueEntry.ACTIONS.DOC != action.action ) {
            logger.info("-- " + action.action
                    + " on Fragment: \"" + getFragment(action).getClass().getSimpleName() + "\""
                    + ( action.viewId != -1 ? " View: " + (null == view ? action.viewId + " (NOT FOUND)" :
                    logName((View) view)
                            + (  view instanceof android.widget.TextView
                            ? " Text='" + ((android.widget.TextView) view).getText().toString() + "'"
                            : "" ))
                    : "" )
            );
        }
        switch (action.action) {
            case CLICK:
                executeClick(action, actionQueue);
                break;
            case LONG_CLICK:
                executeLongClick(action, actionQueue);
                break;
            case SET_TEXT:
                executeSetText(action, actionQueue);
                break;
            case GET_TEXT:
                executeGetText(action, actionQueue);
                break;
            case ASSERT_TEXT:
                executeAssertText(action, actionQueue);
                break;
            case IS_VISIBLE:
                executeIsVisible(action, actionQueue);
                break;
            case WAIT_FOR_VISIBLE:
                executeWaitForVisible(action, actionQueue);
                break;
            case DELAY:
                executeDelay(action, actionQueue);
                break;
            case CALL_BACK:
                executeCallback(action, actionQueue);
                break;
            case DOC:
                executeDoc(action, actionQueue);
                break;
            case TEST_BLOCK:
                executeTestBlock(action, actionQueue);
                break;
            default:
                throw new IllegalArgumentException("Unknown action: " + action.action);
        }
        // post-action wait time
        try {
            Thread.sleep(action.waitTimeMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during pre-action delay", e);
        }
        if ( ActionQueueEntry.ACTIONS.TEST_BLOCK == action.action ) {
            logger.info("---------------------------------------");
        }
    }



    private Fragment getFragment(ActionQueueEntry action) {
        Fragment fragment = RuntimeTester.findFragment(action.fragmentId);
        if (fragment == null) {
            throw new IllegalArgumentException("Fragment not found: " + action.fragmentId);
        }
        return fragment;
    }

    private View getViewOptional(ActionQueueEntry action) {
        Fragment fragment = getFragment(action);
        return fragment.requireView().findViewById(action.viewId);
    }
    private View getView(ActionQueueEntry action) {
        View view = getViewOptional(action);
        if (view == null) {
            logger.warn("View not found: {} in fragment {}", action.viewId, action.fragmentId);
        }
        return view;
    }



    private FragmentActivity getActivity(ActionQueueEntry action) {
        Fragment fragment = getFragment(action);
        if (fragment.getActivity() == null) {
            throw new IllegalArgumentException("Fragment's activity is null: " + action.fragmentId);
        }
        return fragment.getActivity();
    }

    /*
        A C T I O N  E X E C U T O R  M E T H O D S
     */

    private void executeTestBlock(ActionQueueEntry action, ActionQueue actionQueue) {
        // do nothing, just a marker
        // check if on root
        if (!EntryTree.isRootKey( Entries.getCurrentEntryKey() )){
            assertFail("Block ends not in root (current entry: " + Entries.getCurrentEntryKey() + ")");
        }
        testContext = action.text;
        logger.info("=      TEST BLOCK: " + action.text ); // + " ===");
    }

    private void executeDoc(ActionQueueEntry action, ActionQueue actionQueue) {
        // do nothing, just a marker
        logger.info("DOC: {}", action.text);
    }

    private void executeClick(ActionQueueEntry action, ActionQueue actionQueue) {

        if (ActionQueue.ID_BACK == action.viewId) {
            // special case for back button
            logger.info("Performing back press");
            // calls Fragment.onPause() - NOT MainActivity.onBackPressed()
            // getActivity(action).runOnUiThread(() -> getActivity(action).getOnBackPressedDispatcher().onBackPressed());
            MainActivity.getInstance().runOnUiThread(() -> MainActivity.getInstance().onBackPressed());
            return;
        }
        if (ActionQueue.ID_UP == action.viewId) {
            // special case for home button
            logger.info("Performing navigate up");
            //getActivity(action).runOnUiThread(() -> getActivity(action).onNavigateUp());
            MainActivity.getInstance().runOnUiThread(() -> MainActivity.getInstance().onNavigateUp());
            return;
        }
        // Fragment fragment = getFragment(action);
        // View view = fragment.requireView().findViewById(action.viewId);
        View view = UtilDebug.getView(action.viewId);
        if (view instanceof Button || view instanceof android.widget.ImageButton
                || view instanceof androidx.appcompat.widget.AppCompatImageButton
            ) {
            getActivity(action).runOnUiThread(view::performClick);
        } else { // assume it's a MenuItem from the top menu
            MenuItem item = MainActivity.getInstance().menu.findItem(action.viewId);
            if (item != null) {
                getActivity(action).runOnUiThread(() -> MainActivity.getInstance().onOptionsItemSelected(item));
            } else {
                // throw new IllegalArgumentException("View is not a Button or MenuItem: " + action.viewId);
                logger.warn("View is not a Button or MenuItem: " + action.viewId);
            }
        }
    }

    private void executeLongClick(ActionQueueEntry action, ActionQueue actionQueue) {
        getActivity(action).runOnUiThread(() -> UtilDebug.getView(action.viewId).performLongClick());
    }

    private void executeSetText(ActionQueueEntry action, ActionQueue actionQueue) {
        View view = UtilDebug.getView(action.viewId);
        if( null == view ) {
            assertFail("View is null for SET_TEXT action: " + action.viewId);
        } else if (view instanceof android.widget.TextView) {
            new Handler(Looper.getMainLooper()).post(() -> {
                // Perform UI updates here
                ((android.widget.TextView) view).setText(action.text);
            });
        } else  {
            assertFail("View is not a TextView for SET_TEXT action: " + action.viewId);
            // throw new IllegalArgumentException("View is not a TextView: " + action.viewId);
        }
    }

    private void executeGetText(ActionQueueEntry action, ActionQueue actionQueue) {
        View view = UtilDebug.getView(action.viewId);
        if ( null == view ) {
            assertFail("View is null for GET_TEXT action: " + action.viewId);
        } else if (view instanceof android.widget.TextView) {
            String text = ((android.widget.TextView) view).getText().toString();
            actionQueue.setLastRetrievedText(text);
        } else {
            assertFail("View is not a TextView for GET_TEXT action: " + action.viewId);
            //throw new IllegalArgumentException("View is not a TextView: " + action.viewId);
        }
    }

    private void executeAssertText(ActionQueueEntry action, ActionQueue actionQueue) {
        String lastText = actionQueue.getLastRetrievedText();
        if (!action.text.equals(lastText)) {
            assertFail("Text assertion failed: expected '" + action.text + "', got '" + lastText + "'");
            //throw new AssertionError("Text assertion failed: expected '" + action.text + "', got '" + lastText + "'");
        }
    }

    private void executeIsVisible(ActionQueueEntry action, ActionQueue actionQueue) {
        View view = UtilDebug.getView(action.viewId);
        boolean isVisible =  null != view && view.getVisibility() == View.VISIBLE;
        if (!isVisible) {
            assertFail("View is not visible: " + action.viewId);
            // throw new AssertionError("View is not visible: " + action.viewId);
        }
    }

    private void executeWaitForVisible(ActionQueueEntry action, ActionQueue actionQueue) {
        View view = UtilDebug.getView(action.viewId);
        long startTime = System.currentTimeMillis();
        long timeout = action.waitTimeMs > 0 ? action.waitTimeMs : 5000; // default 5 seconds
        while (System.currentTimeMillis() - startTime < timeout) {
            if (null == view) {
                view = UtilDebug.getView(action.viewId);
            } else if (view.getVisibility() == View.VISIBLE) {
                if (null == action.text) {
                    logger.info(logName(view) + " is visible.");
                    return;
                } else if (view instanceof android.widget.TextView) {
                    String currentText = ((android.widget.TextView) view).getText().toString();
                    if (action.text.equals(currentText)) {
                        logger.info(logName(view) + " is visible with expected text: '" + action.text + "'.");
                        return;
                    }
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for visibility", e);
            }
        }

        assertFail("View did not become visible within timeout: " + action.viewId
                + ( null != action.text ? " with text: '" + action.text + "'" : ""));
        UtilDebug.inspectView();
        //throw new AssertionError("View did not become visible within timeout: " + action.viewId);
    }

    private void executeDelay(ActionQueueEntry action, ActionQueue actionQueue) {
        try {
            Thread.sleep(action.waitTimeMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during delay", e);
        }
    }

    private void executeCallback(ActionQueueEntry action, ActionQueue actionQueue) {
        if (action.callback != null) {
            action.callback.onActionCompleted(action);
        } else {
            throw new IllegalArgumentException("Callback is null for CALL_BACK action");
        }
    }


    /*
        assert fail method
     */

    private void assertFail(String message) {
        errorMsg.add( Util.appendIfFilled(testContext,": ") + message +
                " (at " + currentAction.sourceCodeLine + ")" );
        //throw new AssertionError(message);
    }




}
