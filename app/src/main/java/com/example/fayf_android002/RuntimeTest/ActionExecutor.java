package com.example.fayf_android002.RuntimeTest;

import androidx.fragment.app.FragmentActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.view.View;
import androidx.fragment.app.Fragment;

import java.util.Objects;

public class ActionExecutor {

    Logger logger = LoggerFactory.getLogger(ActionExecutor.class);

    public void executeAction(ActionQueueEntry action, ActionQueue actionQueue) {
        // switch on action type
        logger.info("Executing action: " + action.action + " on Fragment: " + action.fragmentId + " View: " + action.viewId
                    + ( action.viewId != -1 ? " (" + getView(action).getClass().getSimpleName() + ")"
                            + (  getView(action) instanceof android.widget.TextView
                                ? " Text='" + ((android.widget.TextView) getView(action)).getText().toString() + "'"
                                    : "" )
                        : "" )
                );
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
            default:
                throw new IllegalArgumentException("Unknown action: " + action.action);
        }
    }


    private Fragment getFragment(ActionQueueEntry action) {
        Fragment fragment = RuntimeTester.findFragment(action.fragmentId);
        if (fragment == null) {
            throw new IllegalArgumentException("Fragment not found: " + action.fragmentId);
        }
        return fragment;
    }
    private View getView(ActionQueueEntry action) {
        Fragment fragment = getFragment(action);
        View view = Objects.requireNonNull(fragment.getView()).findViewById(action.viewId);
        if (view == null) {
            throw new IllegalArgumentException("View not found: " + action.viewId + " in fragment " + action.fragmentId);
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

    private void executeClick(ActionQueueEntry action, ActionQueue actionQueue) {
        getActivity(action).runOnUiThread(() -> getView(action).performClick());
    }

    private void executeLongClick(ActionQueueEntry action, ActionQueue actionQueue) {
        getActivity(action).runOnUiThread(() -> getView(action).performLongClick());
    }

    private void executeSetText(ActionQueueEntry action, ActionQueue actionQueue) {
        View view = getView(action);
        if (view instanceof android.widget.TextView) {
            ((android.widget.TextView) view).setText(action.text);
        } else {
            throw new IllegalArgumentException("View is not a TextView: " + action.viewId);
        }
    }

    private void executeGetText(ActionQueueEntry action, ActionQueue actionQueue) {
        View view = getView(action);
        if (view instanceof android.widget.TextView) {
            String text = ((android.widget.TextView) view).getText().toString();
            actionQueue.setLastRetrievedText(text);
        } else {
            throw new IllegalArgumentException("View is not a TextView: " + action.viewId);
        }
    }

    private void executeAssertText(ActionQueueEntry action, ActionQueue actionQueue) {
        String lastText = actionQueue.getLastRetrievedText();
        if (!action.text.equals(lastText)) {
            throw new AssertionError("Text assertion failed: expected '" + action.text + "', got '" + lastText + "'");
        }
    }

    private void executeIsVisible(ActionQueueEntry action, ActionQueue actionQueue) {
        View view = getView(action);
        boolean isVisible = view.getVisibility() == View.VISIBLE;
        if (!isVisible) {
            throw new AssertionError("View is not visible: " + action.viewId);
        }
    }

    private void executeWaitForVisible(ActionQueueEntry action, ActionQueue actionQueue) {
        View view = getView(action);
        long startTime = System.currentTimeMillis();
        long timeout = action.waitTimeMs > 0 ? action.waitTimeMs : 5000; // default 5 seconds
        while (System.currentTimeMillis() - startTime < timeout) {
            if (view.getVisibility() == View.VISIBLE) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for visibility", e);
            }
        }
        throw new AssertionError("View did not become visible within timeout: " + action.viewId);
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

}
