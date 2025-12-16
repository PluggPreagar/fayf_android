package com.example.fayf_android002.UI;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
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

    private float startX;
    private static final int HORIZONTAL_THRESHOLD = 50; // Adjust threshold as needed

    private GestureDetector gestureDetector;

    public CustomNestedScrollView(Context context) {
        super(context);
        init(context);
    }

    public CustomNestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(velocityY) > Math.abs(velocityX)) {
                    if (velocityY > 0) {
                        // Fling down
                        onFlingDown();
                    } else {
                        // Fling up
                        onFlingUp();
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // allow small vertical movements on swipe, without scrolling the NestedScrollView
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX(); // Record the starting position
                break;

            case MotionEvent.ACTION_MOVE:
                float deltaX = Math.abs(ev.getX() - startX);
                // Prevent NestedScrollView from intercepting the touch event
                if (deltaX > HORIZONTAL_THRESHOLD) {
                    return false;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private void onFlingUp() {
        // Handle fling up
    }

    private void onFlingDown() {
        // Handle fling down
    }
}