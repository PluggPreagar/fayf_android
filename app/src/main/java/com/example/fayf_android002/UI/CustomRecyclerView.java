package com.example.fayf_android002.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class CustomRecyclerView extends RecyclerView {

    Logger logger = org.slf4j.LoggerFactory.getLogger(CustomRecyclerView.class);

    private float startX=0;
    private static final int HORIZONTAL_THRESHOLD = 50; // Adjust threshold as needed

    private GestureDetector gestureDetector;

    public CustomRecyclerView(@NotNull Context context) {
        super(context);
        init(context);
    }

    public CustomRecyclerView(@NotNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomRecyclerView(@NotNull Context context,@Nullable AttributeSet attrs, int defStyleAttr) {
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
                if (startX == -1) {
                    // Already determined to be horizontal scroll
                    return false;
                }
                float deltaX = Math.abs(ev.getX() - startX);
                // Prevent NestedScrollView from intercepting the touch event
                if (deltaX > HORIZONTAL_THRESHOLD) {
                    startX = -1; // flag to indicate horizontal scroll
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