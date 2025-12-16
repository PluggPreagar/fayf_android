package com.example.fayf_android002.UI;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.widget.NestedScrollView;
import com.example.fayf_android002.Entry.Entries;
import com.example.fayf_android002.FirstFragment;
import com.example.fayf_android002.MainActivity;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import com.example.fayf_android002.Util;
import org.slf4j.Logger;

public class CustomNestedScrollView extends NestedScrollView {

    Logger logger = org.slf4j.LoggerFactory.getLogger(CustomNestedScrollView.class);
    private static final long SCROLL_TIMEOUT_MS = 200;

    MotionEventFixed eventFixed = null; // for debugging
    private float SCROLL_THRESHOLD = 10.0f;
    private CustomOnTouchListener viewTouchedInProgress = null;
    private boolean scrolling = false;

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
        /*
            * Custom onTouchEvent to handle delegation to special TouchListener
            * catch and preserve ACTION_DOWN
            * wait for movement or timeout to decide if scrolling UP/DOWN
            *
            * if scrolling, do not delegate to TouchListener
            * else delegate to TouchListener and serve ACTION_DOWN and current event
            *
         */
        logger.debug("CustomNestedScrollView onTouchEvent: " + eventFixed);
        boolean handled = false; // default to not handled
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // capture ACTION_DOWN - may be served later to TouchListener
            eventFixed = new MotionEventFixed(event);// for debugging
        } else if(!scrolling) {
            if (null != viewTouchedInProgress) {
                handled = viewTouchedInProgress.onTouch(eventFixed);
            } else {
                // check if scrolling started, nothing happend
                // or definitely moving differently
                eventFixed.setOtherEvent(event);
                float deltaX = Math.abs(event.getX() - eventFixed.getX());
                float deltaY = Math.abs(event.getY() - eventFixed.getY());
                scrolling = (deltaY > deltaX && deltaY > SCROLL_THRESHOLD);
                // if scrolling started, do not delegate to TouchListener
                // if moving horizontally only, delegate to TouchListener
                // if nothing happened, wait for next event?
                if (!scrolling) {
                    // not scrolling, check if already clear to delegate
                    if (eventFixed.hasMoved() || eventFixed.getDuration() > SCROLL_TIMEOUT_MS) {
                        // moved enough or timeout reached - delegate to TouchListener
                        // serve ACTION_DOWN first
                        viewTouchedInProgress = Entries.getViewTouchedInProgress();
                        handled = viewTouchedInProgress.onTouch(eventFixed);
                        handled = viewTouchedInProgress.onTouch( new MotionEventFixed( event));
                    } else {
                        // set timeout to handle onClik by viewTouchedInProgress
                        Util.postDelayed(() -> {
                            if (!scrolling && null != eventFixed && null == viewTouchedInProgress) {
                                logger.debug("CustomNestedScrollView onTouchEvent: timeout reached, delegating to TouchListener: " + eventFixed);
                                viewTouchedInProgress = Entries.getViewTouchedInProgress();
                                eventFixed.setEventTime(System.currentTimeMillis());
                                boolean handledTimeout = viewTouchedInProgress.onTouch(eventFixed);
                            }
                        }, SCROLL_TIMEOUT_MS);
                    }
                }
            }
        }

        if (!handled) {
            handled = super.onTouchEvent(event);
        }
        return handled;
    }
}