package com.example.fayf_android002;

import android.view.MotionEvent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.fayf_android002.UI.ButtonTouchable;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@org.robolectric.annotation.Config(manifest = Config.NONE)
@RunWith(AndroidJUnit4.class)
public class OnTouchListenerTest extends TestCase {

    public long downTime = System.currentTimeMillis();
    public long enventTimeLast = downTime;
    private FirstFragment firstFragment;
    private TestableOnTouchListener oTL;
    private ButtonTouchable button;
    private float lastX, lastY ;

    public MotionEvent createMotionEvent(int action, long downTime, long eventTime, float x, float y) {
        if (downTime > 100000000) {
            this.downTime = downTime;
        } else if (0 == downTime) {
            downTime = this.downTime;
        } else {
            this.downTime = downTime;
        }
        if (eventTime > 100000000) {
            // Absolute event time
        } else if (0 == eventTime) {
            eventTime = this.enventTimeLast + 100; // Default to 100ms after last event
        } else {
            eventTime = enventTimeLast + eventTime;
        }
        x = (0 <= x) ? this.lastX + x : x;
        y = (0 <= y) ? this.lastY + y : y;
        this.enventTimeLast = eventTime;
        this.lastX = x;
        this.lastY = y;
        return MotionEvent.obtain(
                downTime,  // The time (in ms) when the user originally pressed down
                eventTime, // The time (in ms) when this specific event occurred
                action,    // The action type (e.g., MotionEvent.ACTION_DOWN, ACTION_MOVE, ACTION_UP)
                x,         // X coordinate of the event
                y,         // Y coordinate of the event
                0          // Meta state (e.g., keyboard modifiers, not needed here)
        );
    }


    public void eventDown(long delay, float x, float y) {
        oTL.onTouch(button
                , createMotionEvent(MotionEvent.ACTION_DOWN, 0, delay, x, y));
    }
    public void eventMove(long delay, float x, float y) {
        oTL.onTouch(button
                , createMotionEvent(MotionEvent.ACTION_MOVE, 0, delay, x, y));
    }
    public void eventUp(long delay, float x, float y) {
        oTL.onTouch(button
                , createMotionEvent(MotionEvent.ACTION_UP, 0, delay, x, y));
    }
    public void eventCancel(long delay, float x, float y) {
        oTL.onTouch(button
                , createMotionEvent(MotionEvent.ACTION_CANCEL, 0, delay, x, y));
    }

    /*
        T E S T  H E L P E R S
     */

    private void assertCalled(String msg, String methodName) {
        assertEquals(msg, "called", oTL.getState().get(methodName));
    }

    private void assertNotCalled(String msg, String methodName) {
        assertNull(msg, oTL.getState().get(methodName));
    }

    private void assertState(String msg, String key, String expected) {
        assertEquals(msg, expected, oTL.getState().get(key));
    }

    /*
        T E S T
     */

    @Before
    public void setUp() {
        oTL = new TestableOnTouchListener();
    }

    @Test
    public void testClick() {
        eventDown(0, 100, 100);    // Touch down at (100, 100)
        eventUp(100, 0, 0);    // Touch up at (100, 100) after 100ms
        assertCalled("click w/o move", "onClick()");
    }

    @Test
    public void testClickNotLong() {
        eventDown(149620899, 390, 100);    // Touch down at (100, 100)
        eventCancel(149621027 , 498, 714);    // Touch up at (100, 100) after 100ms
        assertNotCalled("no click", "onClick()");
        assertNotCalled("no long click", "onLongClick()");
    }


}