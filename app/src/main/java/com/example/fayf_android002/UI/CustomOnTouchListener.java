package com.example.fayf_android002.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.fayf_android002.Entry.Entries;
import com.example.fayf_android002.FirstFragment;
import com.example.fayf_android002.MainActivity;
import com.example.fayf_android002.R;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CustomOnTouchListener implements View.OnTouchListener {

    private final int LONG_PRESS_DURATION = 750;
    Logger logger = LoggerFactory.getLogger(CustomOnTouchListener.class);

    private static final int MOVE_THRESHOLD_START = 100;
    private static final int MOVE_THRESHOLD_MOVING = 5;

    private static final int MOVE_TRIGGER_THRESHOLD = 100;

    private static final int SWIPE_VELOCITY_THRESHOLD = 30;
    private MotionEventFixed firstEvent;
    private MotionEventFixed lastEvent;
    private ViewGroup.MarginLayoutParams params_initial = null ;
    private StateListAnimator sla_initial = null;
    private int x_start = -1;
    private int y_start = -1;

    private final Fragment fragment;
    private View  view;
    protected boolean longPressDetected = false;
    protected float deltaX = 0;
    protected float deltaY = 0;
    private float velocityX = 0;
    private float velocityY = 0;
    protected float swipeVelocity = 0;
    public static boolean isDirectionX = false; // need MainActivity to get direction
    protected boolean touching;
    protected boolean isMoveStarted = false;
    protected boolean isMoving;
    private ViewGroup.MarginLayoutParams params = null;
    protected int LONG_PRESS_RELEASE_AUTO_TIMEOUT;
    private int max_margin = 0;

    public CustomOnTouchListener(Fragment ma) {
        this.fragment = ma;
    }

    public long getId() {
        return  null == view ? View.NO_ID : view.getId();
    }


    // calculate velocity and direction
    private boolean calculateVelocityAndDirection(MotionEventFixed e1, MotionEventFixed e2){
        float deltaXcur = e2.getRawX() - e1.getRawX();
        float deltaYcur = e2.getRawY() - e1.getRawY();
        logger.trace("calculateVelocityAndDirection: dXcur={}, dYcur={}",
                String.format("%4d", (int) deltaXcur),
                String.format("%4d", (int) deltaYcur)
        );
        deltaX += deltaXcur;
        deltaY += deltaYcur;
        float absDeltaMax = Math.max(Math.abs(deltaX), Math.abs(deltaY));
        //float absDeltaMax = Math.max(Math.abs(deltaXcur), Math.abs(deltaYcur));
        if (absDeltaMax < (isMoveStarted ? MOVE_THRESHOLD_MOVING : MOVE_THRESHOLD_START)) {
            logger.info("move no: .delta*={} < {}", absDeltaMax , MOVE_THRESHOLD_START);
            return false; // debounce move detection
        } else {
            if (!isMoveStarted) {
                calculateLongPress(e1); // check for long press before move started
                isMoveStarted = true;
            }
            long deltaTime = e2.getEventTime() - e1.getEventTime();
            if (!isDirectionX) {
                isDirectionX = Math.abs(deltaX) > Math.abs(deltaY);
                if (isDirectionX){
                    logger.info("fix direction once set - prevent scrolling");
                }
            }
            float velocityAbs = Math.abs(isDirectionX ? deltaYcur : deltaXcur) / deltaTime * 1000;
            if (velocityAbs > swipeVelocity && velocityAbs > SWIPE_VELOCITY_THRESHOLD) {
                swipeVelocity = velocityAbs;
            }
            logger.info("moved  : dX={}, dY={}, v={}, isDirX={} ({})",
                    String.format("%4d", (int) deltaX),
                    String.format("%4d", (int) deltaY),
                    String.format("%4d", (int) swipeVelocity),
                    isDirectionX,
                    // lookup MotionEvent action names
                    e2.getAction() == MotionEvent.ACTION_DOWN ? "DOWN"     // 0
                            : e2.getAction() == MotionEvent.ACTION_UP ? "UP"     // 1
                            : e2.getAction() == MotionEvent.ACTION_MOVE ? "MOVE" // 2
                            : "OTHER"
            );
        }
        // debounce move detection
        return Math.max(Math.abs(deltaXcur), Math.abs(deltaYcur)) >= MOVE_THRESHOLD_MOVING;
    }

    private void calculateAbsoluteDelta(MotionEventFixed e2){
        if (Math.abs(deltaX) < MOVE_THRESHOLD_START) {
            deltaX = 0;
        }
        if (Math.abs(deltaY) < MOVE_THRESHOLD_START) {
            deltaY = 0;
        }
        logger.info("absolute delta calculated: .deltaX={}, .deltaY={}", this.deltaX, this.deltaY);
    }


    // calculate if initial touch was long press
    private void calculateLongPress(MotionEventFixed e){
        if (!isMoveStarted && !longPressDetected) {
            long pressDuration = e.getEventTime() - e.getDownTime();
            if (pressDuration > LONG_PRESS_DURATION) {
                longPressDetected = true;
                logger.info("Long press detected, duration: {} ms", pressDuration);
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    onLongClickDuringMove(); // raise only once
                }
            } else {
                logger.info("No long press detected, duration: {} ms (to fast)", pressDuration);
            }
        } else {
            logger.info("No long press detected, move started or already detected");
        }
    }

    private void checkLongPressByTimeout(CustomOnTouchListener listener){
        if (!listener.isMoveStarted && !listener.longPressDetected & null !=listener.firstEvent) {
            // make sure no move happened
            // and still in touch
            listener.longPressDetected = true;
            MainActivity.getInstance().runOnUiThread(() -> {
                logStatusDetail();
                listener.onLongClick();
            });
        }
    }

    /*
        allow async evaluation of long press after timeout
     */
    private boolean getTouching(){
        return touching;
    }

    private boolean isMoveStarted(){
        return isMoveStarted;
    }

    private boolean isDirectionX(){
        return isDirectionX;
    }


    private void initLongPressCheckByTimeout(View v){
        LONG_PRESS_RELEASE_AUTO_TIMEOUT = 2000;
        new android.os.Handler().postDelayed(() -> {
            checkLongPressByTimeout(this);
        }, LONG_PRESS_RELEASE_AUTO_TIMEOUT);
        new android.os.Handler().postDelayed(() -> {
            if (getTouching()) { // only if not yet released - that would have triggered onLongClick already
                if (!isMoveStarted()) {
                    onLongPressDelayReached();
                } else {
                    logger.info("Long press delay reached, but move for scrolling - not triggering long press visual");
                }
            } else {
                logger.info("Long press delay reached, but touch already released");
            }
        }, LONG_PRESS_DURATION);
    }

    public boolean onTouch(MotionEventFixed eventFixed) {
        return onTouchAsync(eventFixed);
    }

    public boolean onTouch(View v, MotionEvent event){
        if (null != v) {
            view = v; // KLUDGE
        }
        return onTouchAsync(new MotionEventFixed(event));
    }


    public boolean onTouchAsync(MotionEventFixed eventFixed) {
        new Handler(Looper.getMainLooper()).post(() -> {
            onTouch_(view, eventFixed);
        });
        return true; // consume event
    }


    public boolean onTouch_(View v, MotionEventFixed event){
        // move btn to left
        if (FirstFragment.isScrollingInProgress()) {
            logger.debug("touch ignored( Scrolling in progress ): {}", event);
            return true; // do not consume event
        }
        logger.info("onTouch_ called {}", event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            logger.info("Action Down detected {}", event);
            // reset everything
            swipeVelocity = 0;
            view = v;
            Entries.registerTouchInProgress( this );
            max_margin = null == v ? 0 : (int) (0.75 * v.getWidth());
            sla_initial = null;
            isMoving = false;
            isMoveStarted = false; // reset move
            isDirectionX = false; // reset direction
            touching = true;
            firstEvent = event; // store initial event as copy
            x_start = (int) event.getRawX(); // as event has no fixed values -- REMOVE ??
            y_start = (int) event.getRawX();
            deltaX = 0;
            deltaY = 0;
            lastEvent = firstEvent; // store last event as copy
            longPressDetected = false;
            onTouchDown(v);
            fixateLayout(v);
            initLongPressCheckByTimeout(v);
        } else if (null == firstEvent) {
            logger.warn("First event is null on move/up action");
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (calculateVelocityAndDirection(lastEvent, event)) {
                // debounce
                ;
                // move button according to deltaX - absolute
                if (isDirectionX) {
                    if (Math.abs(deltaX) > MOVE_THRESHOLD_START) {
                        if ( 0 == max_margin || Math.abs(deltaX) < max_margin ) {
                            // keep minimum size
                            float dXMarginLeft = params.leftMargin;
                            params.leftMargin = deltaX > 0 ? (int) deltaX : 0;
                            params.rightMargin = deltaX < 0 ? (int) -deltaX : 0;
                            dXMarginLeft = params.leftMargin - dXMarginLeft; // actual change to compensate changing starting point
                            //
                            updateLayout(v);
                        }
                    }
                }
                // change color of button if moved more than threshold
                if (isDirectionX && isMoveStarted && !isMoving
                        && (deltaX > MOVE_THRESHOLD_START || deltaX < -MOVE_THRESHOLD_START)) {
                    isMoving = true; // onMoving may reset isMoving
                    onMovingX( v, (deltaX < 0 ? - 1 : 1)
                            *  (Math.abs(deltaX) < MOVE_TRIGGER_THRESHOLD ? 1 : 2));
                    logger.info("moved : dX={} -> button color change {}", deltaX, UtilDebug.getBackgroundColorOfButton(v) );
                }
            }
            // iterate
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            logger.debug("Action Up detected {}", event.toString());
            // log all variables
            handleTouchEnd(v, event);
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            logger.info("Action Cancel detected .. wait vor dispatched events {} ", event.toString());
            logStatusDetail();
            // schedule reset after short delay to allow dispatched events to arrive
            long cancelTime =  event.getEventTime();
            new android.os.Handler().postDelayed(() -> {
                MainActivity.getInstance().runOnUiThread(() -> {
                    if (cancelTime < event.getEventTime()) {
                        logger.info("Action Cancel ignored - later event detected {}", lastEvent.toString());
                        return; // ignore cancel as later event detected
                    }
                    logger.info("Action Cancel processed after delay {}", event.toString());
                    handleTouchEnd(v, event);
                });
            }, 100); // robust for short leaving or hand over to CustomNestedScrollView
            // --- always reset after cancel
            // TODO ... handle forwarded from CustomNestedScrollView ?
        } else {
            logger.warn("Unknown action detected: {}", event.toString());
            handleTouchEnd(v, event);
        }
        return isDirectionX; // consume event if moved in X direction
        //return false; // allow other events like onClick to be processed
        //return true; // consume event
    }

    private void handleTouchEnd(View v, MotionEventFixed event) {
        if (touching){

            touching = false;
            calculateLongPress(event); // check for long press on release, if moved it already was checked
            calculateVelocityAndDirection(lastEvent, event);
            calculateAbsoluteDelta(event);
            logStatusDetail();
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
            } else if (isMoveStarted) {
                logger.info("Touch released after move - no *click detected");
            } else if (longPressDetected){
                onLongClick();
            } else {
                onClick();
            }
        } // touching
        Entries.unregisterTouchInProgress( this ); // reset
        resetPosition(v);
        firstEvent = null; // reset
    }


    protected void logStatusDetail(){
        logger.info("CustomOnTouchListener status: " +
                "isMoving=" + isMoving +
                ", isMoveStarted=" + isMoveStarted +
                ", isDirectionX=" + isDirectionX +
                ", longPressDetected=" + longPressDetected +
                ", deltaX=" + deltaX +
                ", deltaY=" + deltaY +
                ", swipeVelocity=" + swipeVelocity
        );
    }


    protected void fixateLayout(View v) {
        // fixate button size
        params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        params_initial = new ViewGroup.MarginLayoutParams(params); // store copy of initial params
        params.height = v.getHeight(); // keep height - even if it is wrap content on shrink
        updateLayout(v);
    }

    public void updateLayout(View v) {
        MainActivity.getInstance().runOnUiThread (() -> {
            updateLayout_(v);
        });
    }

    protected void updateLayout_(View v) {
        // logger.info("updateLayout called - margin l/r {}, {}", params.leftMargin, params.rightMargin);
        v.setLayoutParams(params);
    }


    public void resetPosition(View v){
        // logger.info("SKIPP resetPosition");
        MainActivity.getInstance().runOnUiThread (() -> {
            // run on UI thread
            resetPosition_(v);
        });
    }


    public void resetPosition_(View v){
        logger.info("resetPosition (button vertically) {}, {}", params.leftMargin, params.rightMargin);
        // reset button position
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        if (params.leftMargin>0) params.leftMargin = Math.max(params.leftMargin/2, 5) - 5;
        if (params.rightMargin>0) params.rightMargin = Math.max(params.rightMargin/2 , 5) - 5;
        v.setLayoutParams(params);
        // if margins > 0 -> delayed reset

        if (params.leftMargin>0 || params.rightMargin>0){
            new android.os.Handler().postDelayed(() -> {
                  MainActivity.getInstance().runOnUiThread(() -> {
                  resetPosition(v);
                });
            }, 50);
        }  else{
            if (params_initial != null) {
                v.setStateListAnimator(sla_initial); // restore elevation change on touch
                // or keep color change until next render ?
                //v.setBackgroundColor(MainActivity.getInstance().getColor(R.color.purple_200)); // will be reset by next render, after aktion
                // v.setLayoutParams(params_initial);
            }
            onMovingX( v, 0); // reset color
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

    public void onLongPressDelayReached() {
        // Override this method in your fragment or activity
        logger.info("long press delay reached detected -> show visual effect");
        if (null == sla_initial) {
            // only apply once
            sla_initial = view.getStateListAnimator();
            //view.setStateListAnimator(null); // disable elevation change on touch
            //view.setBackgroundColor(MainActivity.getInstance().getColor(R.color.purple_500));
            //view.setElevation(20f);
            logger.info("long press visual effect applied - background color {}", view.getBackground());
            onHover( view); // strong color
        }
    }


    public void onMovingX(View v, int intensity) {
        MainActivity.getInstance().runOnUiThread (() -> {
            onMovingX_(v, intensity);
        });
    }

    public void onMovingX_(View v, int intensity) {
        // Override this method in your fragment or activity
        int color = intensity < -1 ? R.color.red_700
                : intensity < 0 ? R.color.red_200
                : intensity < 1 ? R.color.black
                : intensity < 2 ? R.color.teal_200
                : R.color.teal_700;
        logger.info("onMovingX intensity={} -> color {}", intensity, color);
        //int colorId = MainActivity.getInstance().getColor(color);
        int colorId = ContextCompat.getColor(MainActivity.getInstance(), color);
        //v.setStateListAnimator(null);
        //v.setBackgroundColor(Color.RED); // orange
        //((Button) v).setTextColor(Color.RED);
        ((Button) v).setTextColor(colorId); // works
        //((Button) v).setShadowLayer(5, 4, 4, colorId);
        //((Button) v).setBackgroundColor(colorId); // does not work ...
        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.TRANSPARENT); // Background color
        if (intensity == 0) {
            border.setStroke(0, Color.TRANSPARENT ); // Border width and color
        } else {
            border.setStroke(4, colorId); // Border width and color
            border.setCornerRadius(32); // Optional: Rounded corners
        }
        v.setBackground(null);
        ((Button) v).setBackground(border);
    }


    public void onHover(View v) {
        int colorId = ContextCompat.getColor(MainActivity.getInstance(), R.color.orange_orange);
        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.TRANSPARENT); // Background color
        border.setStroke(4, colorId); // Border width and color
        border.setCornerRadius(32); // Optional: Rounded corners
        v.setBackground(null);
        ((Button) v).setBackground(border);
    }

    public void onTouchDown(View v) {
        // Override this method in your fragment or activity
        MainActivity.getInstance().runOnUiThread (() -> {
            int colorId = ContextCompat.getColor(MainActivity.getInstance(), R.color.purple_200);
            GradientDrawable border = new GradientDrawable();
            border.setColor(Color.TRANSPARENT); // Background color
            border.setStroke(2, colorId); // Border width and color
            border.setCornerRadius(32); // Optional: Rounded corners
            v.setBackground(null);
            ((Button) v).setBackground(border);
            // fade out after short delay
            fadeOutBorder(v);
        });
    }

    private void fadeOutBorder(View v) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(v, "alpha", 1f, 0.5f);
        fadeOut.setDuration(500); // Duration in milliseconds
        fadeOut.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                v.setBackground(null); // Remove the border after fading out
                v.setAlpha(1f); // Reset alpha for future interactions
            }

        });
        fadeOut.start();
    }


}