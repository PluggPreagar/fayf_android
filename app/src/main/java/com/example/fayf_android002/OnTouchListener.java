package com.example.fayf_android002;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnTouchListener implements View.OnTouchListener {

    Logger logger = LoggerFactory.getLogger(OnTouchListener.class);

    private static final int MOVE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private MotionEvent firstEvent;
    private MotionEvent lastEvent;
    private ViewGroup.MarginLayoutParams params_initial;
    private int x_start;

    private final Fragment fragment;
    protected boolean longPressDetected = false;
    private float deltaX = 0;
    private float deltaY = 0;
    private float velocityX = 0;
    private float velocityY = 0;
    protected float swipeVelocity = 0;
    protected boolean isDirectionX = false;
    protected boolean isMoveStarted = false;
    private ViewGroup.MarginLayoutParams params = null;

    public OnTouchListener(Fragment ma) {
        this.fragment = ma;
    }

    public boolean onTouch_(View v, MotionEvent event) {

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

    // calculate velocity and direction
    private void calculateVelocityAndDirection(MotionEvent e1, MotionEvent e2){
        float deltaX = e2.getX() - e1.getX();
        float deltaY = e2.getY() - e1.getY();
        float absDeltaMax = Math.max(Math.abs(deltaX), Math.abs(deltaY));
        if (absDeltaMax < MOVE_THRESHOLD) {
            logger.info("no move: {} < {}", absDeltaMax , MOVE_THRESHOLD);
            swipeVelocity = 0;
        } else {
            if (!isMoveStarted) {
                calculateLongPress(e1); // check for long press before move started
                isMoveStarted = true;
            }
            long deltaTime = e2.getEventTime() - e1.getEventTime();
            isDirectionX = Math.abs(deltaX) > Math.abs(deltaY);
            float velocityAbs = Math.abs(isDirectionX ? deltaY : deltaX) / deltaTime * 1000;
            if (velocityAbs > swipeVelocity && velocityAbs > SWIPE_VELOCITY_THRESHOLD) {
                swipeVelocity = velocityAbs;
            }
        }
        logger.info("Move detected: .deltaX={}, .deltaY={}, velocity={}, isDirectionX={}", deltaX, deltaY, swipeVelocity, isDirectionX);
    }

    private void calculateAbsoluteDelta(MotionEvent e2){
        float deltaX = e2.getX() - firstEvent.getX();
        float deltaY = e2.getY() - firstEvent.getY();
        if (Math.abs(deltaX) > MOVE_THRESHOLD ) {
            this.deltaX = deltaX;
        }
        if (Math.abs(deltaY) > MOVE_THRESHOLD ) {
            this.deltaY = deltaY;
        }
    }


    // calculate if initial touch was long press
    private void calculateLongPress(MotionEvent e){
        if (!isMoveStarted && !longPressDetected) {
            long pressDuration = e.getEventTime() - e.getDownTime();
            if (pressDuration > 500) {
                longPressDetected = true;
                logger.info("Long press detected, duration: {} ms", pressDuration);
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    onLongClickDuringMove(); // raise only once
                }
            }
        }
    }



    public boolean onTouch(View v, MotionEvent event){
        // move btn to left
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            firstEvent = MotionEvent.obtain(event); // store initial event as copy
            x_start = (int) event.getX(); // as event has no fixed values -- REMOVE ??
            lastEvent = firstEvent; // store last event as copy
            // fixate button size
            params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params_initial = new ViewGroup.MarginLayoutParams(params); // store copy of initial params
            params.height = v.getHeight(); // keep height - even if it is wrap content on shrink
            v.setLayoutParams(params);
        } else if (null == firstEvent) {
            logger.warn("First event is null on move/up action");
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            calculateVelocityAndDirection(lastEvent, event);
            // move button according to deltaX - absolute
            float deltaX = event.getX() - firstEvent.getX();
            params.leftMargin = deltaX > 0 ? (int) deltaX : 0 ;
            params.rightMargin = deltaX < 0 ? (int) -deltaX : 0 ;
            // iterate
            lastEvent = MotionEvent.obtain(event); // store last event as copy
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            calculateLongPress(event); // check for long press on release, if moved it already was checked
            calculateAbsoluteDelta(event);
            if (swipeVelocity > 0) {
                if (isDirectionX) {
                    if (deltaX < 0) {
                        if (longPressDetected) {
                            onLongPressAndSwipeLeft();
                        } else {
                            onSwipeLeft();
                        }
                    } else {
                        if (longPressDetected) {
                            onLongPressAndSwipeRight();
                        } else {
                            onSwipeRight();
                        }
                    }
                } else {
                    if (deltaY < 0) {
                        if (longPressDetected) {
                            onLongPressAndSwipeTop();
                        } else {
                            onSwipeTop();
                        }
                    } else {
                        if (longPressDetected) {
                            onLongPressAndSwipeBottom();
                        } else {
                            onSwipeBottom();
                        }
                    }
                }
            } else if (longPressDetected){
                onLongClick();
            } else {
                onClick();
            }
            resetPosition(v);
            firstEvent = null; // reset
        } else {
            resetPosition(v);
        }
        //return false; // allow other events like onClick to be processed
        return true; // consume event
    }

    public void resetPosition(View v){
        // reset button position
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        if (params.leftMargin>0) params.leftMargin = Math.max(params.leftMargin/2, 5) - 5;
        if (params.rightMargin>0) params.rightMargin = Math.max(params.rightMargin/2 , 5) - 5;
        v.setLayoutParams(params);
        // if margins > 0 -> delayed reset
        if (params.leftMargin>10 || params.rightMargin>10){
            new android.os.Handler().postDelayed(() -> {
                  fragment.requireActivity().runOnUiThread(() -> {
                    resetPosition(v);
                });
            }, 50);
        } else if (params_initial != null) {
            v.setLayoutParams(params_initial);
        }
    }



    public void onSwipeRight() {
        // Override this method in your fragment or activity
        logger.info("Swipe Right detected");
    }

    public void onSwipeLeft() {
        // Override this method in your fragment or activity
        logger.info("Swipe Left detected");
    }

    public void onSwipeTop() {
        // Override this method in your fragment or activity
        logger.info("Swipe Top detected");
    }
    public void onSwipeBottom() {
        // Override this method in your fragment or activity
        logger.info("Swipe Bottom detected");
    }

    public void onLongClick() {
        // Override this method in your fragment or activity
        logger.info("Long Click detected");
    }

    public void onClick() {
        // Override this method in your fragment or activity
        logger.info("Click detected");
    }

    public void onLongClickDuringMove() {
        // Override this method in your fragment or activity
        logger.info("Long Click During Move detected");
    }

    public void onLongPressAndSwipeLeft() {
        // Override this method in your fragment or activity
        logger.info("Long Press and Swipe Left detected");
    }

    public void onLongPressAndSwipeRight() {
        // Override this method in your fragment or activity
        logger.info("Long Press and Swipe Right detected");
    }

    public void onLongPressAndSwipeTop() {
        // Override this method in your fragment or activity
        logger.info("Long Press and Swipe Top detected");
    }

    public void onLongPressAndSwipeBottom() {
        // Override this method in your fragment or activity
        logger.info("Long Press and Swipe Bottom detected");
    }


}