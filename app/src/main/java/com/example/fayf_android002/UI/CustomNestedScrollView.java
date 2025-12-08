package com.example.fayf_android002.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.widget.NestedScrollView;
import com.example.fayf_android002.Entry.Entries;
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
        logger.debug("CustomNestedScrollView onTouchEvent: action={}", event.toString().substring(0,50));
        boolean handled = false; // default to not handled
        // KLUDGE
        View viewTouchedInProgress = Entries.getViewTouchedInProgress();
        if (viewTouchedInProgress != null && false) {
            logger.debug("CustomNestedScrollView delegating onTouchEvent to viewTouchedInProgress");
            MotionEvent eventCopy = MotionEvent.obtain(event);
            handled = viewTouchedInProgress.onTouchEvent(event);
            eventCopy.recycle();
        }
        if (!handled) {
            handled = super.onTouchEvent(event);
        }
        return handled;
    }
}