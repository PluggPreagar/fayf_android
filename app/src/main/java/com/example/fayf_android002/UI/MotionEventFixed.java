package com.example.fayf_android002.UI;

import android.view.MotionEvent;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import org.jetbrains.annotations.NotNull;

// Dummy class to ease debugging MotionEvent issues
public class MotionEventFixed {

    private final int action;
    private final float x;
    private final float y;
    private final long downTime;
    private final long eventTime;
    private final String msg;
    private final float rawX;
    private final float rawY;

    public MotionEventFixed(MotionEvent event) {
        // clone the event with all properties
        this.action = event.getAction();
        this.x = event.getX();
        this.y = event.getY();
        this.rawX = event.getRawX();
        this.rawY = event.getRawY();
        this.downTime = event.getDownTime();
        this.eventTime = event.getEventTime();
        this.msg = UtilDebug.eventToStr(event);
    }

    public int getAction() {
        return action;
    }

    public long getDownTime() {
        return downTime;
    }

    public long getEventTime() {
        return eventTime;
    }

    public @NotNull String toString() {
        return msg;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRawX() {
        return rawX;
    }

    public float getRawY() {
        return rawY;
    }
}
