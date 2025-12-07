package com.example.fayf_android002;

import android.view.MotionEvent;
import android.view.View;
import com.example.fayf_android002.UI.CustomOnTouchListener;

import java.util.Map;
import java.util.TreeMap;

class TestableOnTouchListener extends CustomOnTouchListener {

    Map<String, String> state = new TreeMap<>();

    public TestableOnTouchListener() {
        super(null);
    }

    //

    public boolean onTouch(View v, MotionEvent event) {
        boolean b = super.onTouch(v, event);
        printState("onTouch " + MotionEvent.actionToString(event.getAction()) + " processed");
        return b;
    }

    // Override to prevent side effects during testing

    protected void state(String key, String value) {
        state.put(key, value);
        System.out.printf("state: %s = %s\n", key, value);
    }

    protected void fixateLayout(View v) {
        state("fixateLayout()", "called");
    }

    protected void updateLayout(View v, float dXMarginLeft) {
        state("updateLayout()", "called");
    }

    protected void registerTouchInProgress(View v) {
        state("registerTouchInProgress()", "called");
    }

    protected void unregisterTouchInProgress(View v) {
        state("unregisterTouchInProgress()", "called");
    }

    public void resetPosition(View v) {
        state("resetPosition()", "called");
    }

    public void onClick() {
        state("onClick()", "called");
    }

    public void onLongClick() {
        state("onLongClick()", "called");
    }

    public void onLongPressAndSwipeLeft() {
        state("onLongPressAndSwipeLeft()", "called");
    }

    public void onSwipeLeft() {
        state("onSwipeLeft()", "called");
    }

    public void onLongPressAndSwipeRight() {
        state("onLongPressAndSwipeRight()", "called");
    }

    public void onSwipeRight() {
        state("onSwipeRight()", "called");
    }

    public void onLongPressAndSwipeTop() {
        state("onLongPressAndSwipeTop()", "called");
    }

    public void onSwipeTop() {
        state("onSwipeTop()", "called");
    }

    public void onLongPressAndSwipeBottom() {
        state("onLongPressAndSwipeBottom()", "called");
    }

    public void onSwipeBottom() {
        state("onSwipeBottom()", "called");
    }

    public void onMovingX(View v, int intensity) {
        state("movingX", Integer.toString(intensity));
    }

    public void onLongPressDelayReached() {
        state("onLongPressDelayReached()", "called"); // indicator for user feedback
        super.onLongPressDelayReached();
    }

    /* make field accessible for test */

    public Map<String, String> getState() {
        state.put("deltaX", String.valueOf(this.deltaX));
        state.put("deltaY", String.valueOf(this.deltaY));
        state.put("swipeVelocity", String.valueOf(this.swipeVelocity));
        state.put("isDirectionX", String.valueOf(this.isDirectionX));
        state.put("isMoveStarted", String.valueOf(this.isMoveStarted));
        state.put("isMoving", String.valueOf(this.isMoving));
        return state;
    }

    public void printState(String msg) {
        System.out.printf("----- %s -----\n", msg);
        getState().forEach((k, v) -> {
            System.out.printf("state: %s = %s\n", k, v);
        });
        System.out.println("----- end -----\n");
    }

}
