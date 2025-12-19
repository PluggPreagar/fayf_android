package com.example.fayf_android002.RuntimeTest;

import android.view.View;
import androidx.core.internal.view.SupportMenuItem;
import androidx.fragment.app.Fragment;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ActionQueueEntry {

    Logger logger = org.slf4j.LoggerFactory.getLogger(ActionQueueEntry.class);

    public final ACTIONS action;
    public final int fragmentId;
    public int viewId;
    public String text; // for SET_TEXT action
    public long waitTimeMs; // for WAIT action

    public String sourceCodeLine = ""; // for DOC action
    public View resolvedView = null; // resolved view for action
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


    public @NotNull String toString() {
        StringBuilder str = new StringBuilder();
        String text = "";
        str.append(action.toString());
        // ignore Fragment for now
        Object view = null != resolvedView ? resolvedView : getViewOptional();
        if (null == view ) {
            str.append(" <any view> ");
        } else {
            str.append(" ").append( UtilDebug.logName( (View) view ) );
            // Append visibility
            str.append(" ").append( UtilDebug.getVisibilityStatus( (View) view ) );
            if ( view instanceof android.widget.TextView ){
                text = ((android.widget.TextView) view).getText().toString();
            } else if ( view instanceof SupportMenuItem){
                text = ((SupportMenuItem) view).getTitle().toString();
            }
        }
        str.append( viewId > 0 ? " " + viewId : "")
                .append( this.text != null ? " '" + this.text + "'" : "" )
                .append( resolvedView != null ? " RESOLVED" : (view != null ? " matched" : "" ))
        ;
        if (null != view) {
            List<String> matchList = new ArrayList<>();
            if (viewId == ((View) view).getId()) {
                matchList.add("ID");
            }
            if ( !text.isEmpty() && text.equals(this.text) ) {
                matchList.add("TEXT");
            }
            if (!matchList.isEmpty()) {
                str.append(" (by ").append(String.join(", ", matchList)).append(")");
            } else {
                str.append(" (unknown)");
            }
        }
        return str.toString();
    }



    public Fragment getFragment() {
        Fragment fragment = RuntimeTester.findFragment(fragmentId);
        if (fragment == null) {
            throw new IllegalArgumentException("Fragment not found: " + fragmentId);
        }
        return fragment;
    }


    public View getViewOptional() {
        View view = null;
        if ( viewId < 0 ) {
            // search view by text
            view = UtilDebug.getView(null, text);
        } else {
            view = UtilDebug.getView(viewId);
        }
        if ( null == view ) {
            // try to find in fragment
            if ( viewId >= View.VISIBLE ) {
                Fragment fragment = RuntimeTester.findFragmentOptional(fragmentId);
                if ( null != fragment ) {
                    view = fragment.requireView().findViewById(viewId);
                }
            }
        }
        return  view;
    }

    public View getView() {
        View view = getViewOptional();
        if (view == null) {
            logger.warn("View not found: {} in fragment {}", viewId, fragmentId);
        }
        return view;
    }



}
