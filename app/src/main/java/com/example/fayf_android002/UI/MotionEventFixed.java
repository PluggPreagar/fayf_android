package com.example.fayf_android002.UI;

import android.view.MotionEvent;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import org.jetbrains.annotations.NotNull;

// Dummy class to ease debugging MotionEvent issues
public class MotionEventFixed {

    private static final float MOVE_THRESHOLD = 5.0f;
    private final int action;
    private final float x;
    private final float y;
    private final long downTime;
    private long eventTime; // may be modified on delayed handover ...
    private final String msg;
    private final float rawX;
    private final float rawY;

    private MotionEvent otherEvent = null;

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

    public MotionEventFixed setOtherEvent(MotionEvent other) {
        this.otherEvent = other;
        return this;
    }

    public boolean hasMoved() {
        return null != otherEvent && (this.rawX != otherEvent.getRawX() || this.y != otherEvent.getRawY())
             && Math.max(Math.abs(this.rawX - otherEvent.getRawX()), Math.abs(this.rawY - otherEvent.getRawY()))
                > MOVE_THRESHOLD;
    }

    public long getDuration() {
        return  (null != otherEvent ? otherEvent.getEventTime() : eventTime) - downTime;
    }




    /* Getter methods */

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

    public void setEventTime(long l) {
        eventTime = l;
    }
}
