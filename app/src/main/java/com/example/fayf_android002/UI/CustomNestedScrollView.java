package com.example.fayf_android002.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.widget.NestedScrollView;
import com.example.fayf_android002.Entry.Entries;
import com.example.fayf_android002.FirstFragment;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import com.example.fayf_android002.Util;
import org.slf4j.Logger;

public class CustomNestedScrollView extends NestedScrollView {

    Logger logger = org.slf4j.LoggerFactory.getLogger(CustomNestedScrollView.class);

    public CustomNestedScrollView(Context context) {
        super(context);
    }

    public CustomNestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Pass the touch event to the parent class
        MotionEventFixed eventFixed = new MotionEventFixed(event);// for debugging
        logger.debug("CustomNestedScrollView onTouchEvent: " + eventFixed);
        boolean handled = false; // default to not handled
        // KLUDGE
        CustomOnTouchListener viewTouchedInProgress = Entries.getViewTouchedInProgress();
        if (viewTouchedInProgress != null && !FirstFragment.isScrollingInProgress()) {
            // need to inform about moving-on ...
            logger.debug("CustomNestedScrollView delegating onTouchEvent to viewTouchedInProgress");
            handled = viewTouchedInProgress.onTouch(eventFixed);
        }
        if (!handled) {
            handled = super.onTouchEvent(event);
        }
        return handled;
    }
}