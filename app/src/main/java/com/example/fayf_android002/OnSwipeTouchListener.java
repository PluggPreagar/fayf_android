package com.example.fayf_android002;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnSwipeTouchListener implements View.OnTouchListener {

    Logger logger = LoggerFactory.getLogger(OnSwipeTouchListener.class);

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        logger.info("onTouch event: {}", event.toString());

        /*
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                    return true;
                }
            }
            return false;
        }

     */
        return true;
    }

    public void onSwipeRight() {
        // Override this method in your fragment or activity
    }

    public void onSwipeLeft() {
        // Override this method in your fragment or activity
    }

    public void onSwipeTop() {
        // Override this method in your fragment or activity
    }
    public void onSwipeBottom() {
        // Override this method in your fragment or activity
    }
}