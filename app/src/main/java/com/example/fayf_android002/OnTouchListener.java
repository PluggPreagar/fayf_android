package com.example.fayf_android002;

import android.animation.StateListAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnTouchListener implements View.OnTouchListener {

    private final int LONG_PRESS_DURATION = 500;
    Logger logger = LoggerFactory.getLogger(OnTouchListener.class);

    private static final int MOVE_THRESHOLD_START = 100;
    private static final int MOVE_THRESHOLD_MOVING = 5;

    private static final int MOVE_TRIGGER_THRESHOLD = 100;

    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private MotionEvent firstEvent;
    private MotionEvent lastEvent;
    private ViewGroup.MarginLayoutParams params_initial = null ;
    private StateListAnimator sla_initial = null;
    private int x_start;

    private final Fragment fragment;
    private View  view;
    protected boolean longPressDetected = false;
    private float deltaX = 0;
    private float deltaY = 0;
    private float velocityX = 0;
    private float velocityY = 0;
    protected float swipeVelocity = 0;
    protected static boolean isDirectionX = false; // need MainActivity to get direction
    protected boolean isMoveStarted = false;
    private ViewGroup.MarginLayoutParams params = null;
    private int LONG_PRESS_RELEASE_AUTO_TIMEOUT;
    private boolean touching;

    public OnTouchListener(Fragment ma) {
        this.fragment = ma;
    }

    public boolean onTouch_(View v, MotionEvent event) {

        logger.info("OnTouchListener onTouch event: {}", event.toString());

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
    private boolean calculateVelocityAndDirection(MotionEvent e1, MotionEvent e2){
        float deltaXcur = e2.getX() - e1.getX();
        float deltaYcur = e2.getY() - e1.getY();
        deltaX += deltaXcur;
        deltaY += deltaYcur;
        // float absDeltaMax = Math.max(Math.abs(deltaX), Math.abs(deltaY));
        float absDeltaMax = Math.max(Math.abs(deltaXcur), Math.abs(deltaYcur));
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
            logger.info("moved  : .deltaXcur={}, .deltaYcur={}, velocity={}, isDirectionX={} (action={})"
                    , deltaXcur, deltaYcur, swipeVelocity, isDirectionX
                    // lookup MotionEvent action names
                    , e2.getAction() == MotionEvent.ACTION_DOWN ? "DOWN"     // 0
                            : e2.getAction() == MotionEvent.ACTION_UP ? "UP"     // 1
                            : e2.getAction() == MotionEvent.ACTION_MOVE ? "MOVE" // 2
                            : "OTHER"
            );
        }
        // debounce move detection
        return Math.max(Math.abs(deltaXcur), Math.abs(deltaYcur)) >= MOVE_THRESHOLD_MOVING;
    }

    private void calculateAbsoluteDelta(MotionEvent e2){
        if (Math.abs(deltaX) < MOVE_THRESHOLD_START) {
            deltaX = 0;
        }
        if (Math.abs(deltaY) < MOVE_THRESHOLD_START) {
            deltaY = 0;
        }
        logger.info("absolute delta calculated: .deltaX={}, .deltaY={}", this.deltaX, this.deltaY);
    }


    // calculate if initial touch was long press
    private void calculateLongPress(MotionEvent e){
        if (!isMoveStarted && !longPressDetected) {
            long pressDuration = e.getEventTime() - e.getDownTime();
            if (pressDuration > LONG_PRESS_DURATION) {
                longPressDetected = true;
                logger.info("Long press detected, duration: {} ms", pressDuration);
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    onLongClickDuringMove(); // raise only once
                }
            }
        }
    }

    private void checkLongPressByTimeout(OnTouchListener listener){
        if (!listener.isMoveStarted && !listener.longPressDetected & null !=listener.firstEvent) {
            // make sure no move happened
            // and still in touch
            listener.longPressDetected = true;
            fragment.requireActivity().runOnUiThread(() -> {
                listener.onLongClick();
            });
        }
    }

    private void initLongPressCheckByTimeout(View v){
        LONG_PRESS_RELEASE_AUTO_TIMEOUT = 2000;
        new android.os.Handler().postDelayed(() -> {
            fragment.requireActivity().runOnUiThread(() -> {
                checkLongPressByTimeout(this);
            });
        }, LONG_PRESS_RELEASE_AUTO_TIMEOUT);
        new android.os.Handler().postDelayed(() -> {
            fragment.requireActivity().runOnUiThread(() -> {
                if (touching) { // only if not yet released - that would have triggered onLongClick already
                    if (!isMoveStarted || isDirectionX) {
                        onLongPressDelayReached();
                    } else {
                        logger.info("Long press delay reached, but move for scrolling - not triggering long press visual");
                    }
                }
            });
        }, LONG_PRESS_DURATION);
    }

    public boolean onTouch(View v, MotionEvent event){
        // move btn to left
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            logger.info("Action Down detected {}", event.toString());
            Entries.settingTouchInProgress(v);
            swipeVelocity = 0;
            view = v;
            sla_initial = null;
            isDirectionX = false; // reset direction
            touching = true;
            firstEvent = MotionEvent.obtain(event); // store initial event as copy
            x_start = (int) event.getX(); // as event has no fixed values -- REMOVE ??
            lastEvent = firstEvent; // store last event as copy
            // fixate button size
            params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params_initial = new ViewGroup.MarginLayoutParams(params); // store copy of initial params
            params.height = v.getHeight(); // keep height - even if it is wrap content on shrink
            v.setLayoutParams(params);
            initLongPressCheckByTimeout(v);
        } else if (null == firstEvent) {
            logger.warn("First event is null on move/up action");
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (calculateVelocityAndDirection(lastEvent, event)) {
                // debounce
                lastEvent = MotionEvent.obtain(event); // store last event as copy
                // move button according to deltaX - absolute
                if (isDirectionX) {
                    params.leftMargin = deltaX > 0 ? (int) deltaX : 0;
                    params.rightMargin = deltaX < 0 ? (int) -deltaX : 0;
                }
                // change color of button if moved more than threshold
                if (isDirectionX && (deltaX > MOVE_THRESHOLD_START || deltaX < -MOVE_THRESHOLD_START)) {
                    v.setBackgroundColor(
                            deltaX < 0 ?
                                    (MOVE_TRIGGER_THRESHOLD < -deltaX ?
                                            fragment.requireContext().getColor(R.color.red) :
                                            fragment.requireContext().getColor(R.color.orange_orange))
                                    : (MOVE_TRIGGER_THRESHOLD < deltaX ?
                                    fragment.requireContext().getColor(R.color.teal_700) :
                                    fragment.requireContext().getColor(R.color.teal_200))
                    );
                }
                ;

                v.setLayoutParams(params);
            }
            // iterate
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            logger.info("Action Up detected {}", event.toString());
            touching = false;
            calculateLongPress(event); // check for long press on release, if moved it already was checked
            calculateVelocityAndDirection(lastEvent, event);
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
            Entries.settingTouchInProgressReset(v); // reset
            resetPosition(v);
            firstEvent = null; // reset
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            logger.info("Action Cancel detected {} .. wait vor dispatched events", event.toString());
        } else {
            logger.warn("Unknown action detected: {}", event.toString());
            touching = false;
            Entries.settingTouchInProgressReset(v); // reset
            resetPosition(v);
            firstEvent = null; // reset
        }
        return isDirectionX; // consume event if moved in X direction
        //return false; // allow other events like onClick to be processed
        //return true; // consume event
    }

    public void resetPosition(View v){
        // reset button position
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        if (params.leftMargin>0) params.leftMargin = Math.max(params.leftMargin/2, 5) - 5;
        if (params.rightMargin>0) params.rightMargin = Math.max(params.rightMargin/2 , 5) - 5;
        v.setLayoutParams(params);
        // if margins > 0 -> delayed reset

        if (params.leftMargin>0 || params.rightMargin>0){
            new android.os.Handler().postDelayed(() -> {
                  fragment.requireActivity().runOnUiThread(() -> {
                  resetPosition(v);
                });
            }, 50);
        }  else if (params_initial != null) {
            v.setStateListAnimator(sla_initial); // restore elevation change on touch
            // or keep color change until next render ?
            //v.setBackgroundColor(fragment.requireContext().getColor(R.color.purple_200)); // will be reset by next render, after aktion
            // v.setLayoutParams(params_initial);
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
        logger.info(TextViewAppender.appendLog("Click detected"));
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
        logger.info("Long Press Delay Reached detected");
        if (null == sla_initial) {
            sla_initial = view.getStateListAnimator();
            view.setStateListAnimator(null); // disable elevation change on touch
            view.setBackgroundColor(fragment.requireContext().getColor(R.color.purple_500));
        }
    }




}