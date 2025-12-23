package com.example.fayf_android002.UI;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.core.content.ContextCompat;
import com.example.fayf_android002.Config;
import com.example.fayf_android002.Entry.Entries;
import com.example.fayf_android002.Entry.EntryKey;
import com.example.fayf_android002.FirstFragment;
import com.example.fayf_android002.MainActivity;
import com.example.fayf_android002.R;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import com.google.android.material.button.MaterialButton;
import org.slf4j.Logger;

public class ButtonTouchable extends MaterialButton {

    Logger logger = org.slf4j.LoggerFactory.getLogger(ButtonTouchable.class);

    private EntryKey entryKey = null; // entry associated with this button
    private FirstFragment fragment = null; // fragment containing this button
    private static float startX;
    private Drawable background = null;

    private long lastClickTime = 0;

    private GestureDetector gestureDetector;
    private boolean moveStarted = false;

    // TODO use swipe-fling-top/bottom to move item
    //   lock onto item by inital click/longpress/x-movement(like swipe) and than move up/down to reorder


    // constructor with context, attribute set
    public ButtonTouchable(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return performClick(); // Handle single click
            }

            @Override
            public void onLongPress(MotionEvent e) {
                performLongClick(); // Handle long click
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                onDoubleClick();
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(velocityX) > Math.abs(velocityY)) {
                    if (velocityX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                }
                return true;
            }
        });

        setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //logger.debug("ButtonTouchable onTouchEvent: action={}, x={}", event.getAction(), event.getX());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                moveStarted = false;
                background = getBackground();


                setBackgroundColor(getResources().getColor(android.R.color.darker_gray)); // Light feedback on touch start

//                // Set the red trash bin icon on the right side of the button
//                Drawable trashBinIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_folder_24);
//                if (trashBinIcon != null) {
//                    trashBinIcon.setBounds(0, 0, trashBinIcon.getIntrinsicWidth(), trashBinIcon.getIntrinsicHeight());
//                }
//                setCompoundDrawablesWithIntrinsicBounds(null, null, trashBinIcon, null);

                // Optional: Add padding between the text and the icon
                setCompoundDrawablePadding(16); // show a light movement feedback
                // set border

                // Create a GradientDrawable with a light border
                GradientDrawable borderDrawable = new GradientDrawable();
                borderDrawable.setColor( getContext().getColor(android.R.color.transparent)); // Transparent background
                borderDrawable.setStroke(4, getContext().getColor(android.R.color.darker_gray)); // Light gray border
                borderDrawable.setCornerRadius(16); // Optional: Rounded corners
                // Apply the border drawable
                setBackgroundTintList(null); // Clear any existing tint
                setBackgroundColor( getContext().getColor(R.color.white) );
                setBackgroundTintMode(android.graphics.PorterDuff.Mode.SRC_IN);
                setBackground(borderDrawable); // Apply the border


                break;

            case MotionEvent.ACTION_MOVE:
                // Detect swipe direction dynamically
                float deltaX = event.getX() - startX;
                if (!moveStarted){
                    if (Math.abs(deltaX) < 5 ) {
                        break;
                    }
                    moveStarted = true;
                }
                float maxSwipeDistance = getWidth(); // Use the button's width as the max swipe distance
                // overshoot a bit for full color
                float proportion = Math.min(1, 1.5f * Math.abs(deltaX) / maxSwipeDistance); // Clamp proportion to [0, 1]

                // very low delta -> gray - neutral
                // low delta -> show red, white, green gradient - white where finger is
                // high delta swiped left -> solid red at left, white at opposite edge
                // high delta swiped right -> solid green at right, white at opposite edge

                int leftColor = (deltaX < 10) ? Color.RED : Color.WHITE;
                int middleColor = Color.WHITE;
                int rightColor = (deltaX > -10 ) ? Color.GREEN : Color.WHITE;
                float[] positions = new float[3];
                positions[0] = 0.0f;
                positions[1] = ( startX - deltaX) / getWidth(); // match finger position at start and expand in opposite direction
                positions[2] = 1.0f;
                GradientDrawable gradientDrawable = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{leftColor, middleColor, rightColor}
                );
                gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    gradientDrawable.setColors(new int[]{leftColor, middleColor, rightColor}, positions);
                }


                //gradientDrawable.setGradientCenter( positions[0], positions[1]);
                gradientDrawable.setCornerRadius(32); // Optional: Rounded corners
                //gradientDrawable.setStroke(4, Color.BLACK); // Optional: Border width and color
                //setBackground(gradientDrawable); // Apply the gradient dynamically

                //setBackgroundColor(Color.TRANSPARENT); // Clear solid color to show gradient

                //setBackgroundDrawable(gradientDrawable);

                setBackgroundTintList(null);
                setBackgroundDrawable(gradientDrawable);
                //



                break;

            default: // might not be called when swipe detected etc.
                //logger.debug("ButtonTouchable onTouchEvent: finish action={}", event.getAction());
                resetBackgroundAfterDelay();
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setEntry(EntryKey entryKey, FirstFragment fragment) {
        this.entryKey = entryKey;
        this.fragment = fragment;
    }

    public EntryKey getEntryKey() {
        return entryKey;
    }

    public ButtonTouchable(android.content.Context context) {
        super(context);
    }

    public void setText(String text) {
        super.setText(text);
        assert entryKey != null;
    }



    @Override
    public boolean performClick() {
        if (moveStarted){
            return true; // ignore click if movement was started
        }
        super.performClick();
        // debounce - as we allow to use onSingleTapConfirmed-Trigger as well as performClick directly
        if (System.currentTimeMillis() - lastClickTime < 300) {
            logger.warn("ButtonTouchable performClick debounced.");
            return true; // indicate the click was handled
        }
        lastClickTime = System.currentTimeMillis();
        UtilDebug.logCompactCallStack("ButtonTouchable performClick");
        resetBackgroundAfterDelay();
        if (null == entryKey) {
            logger.error("ButtonTouchable is not initialized!");
            return true; // indicate the click was handled
        }
        assert entryKey != null;
        String fullPath = entryKey.getFullPath();
        logger.info("ButtonTouchable clicked ({}).", fullPath);
        if (Entries.sizeTopic(entryKey) > 0) {
            Entries.setCurrentEntryKey(entryKey); // set topic to this entry
        } else if (entryKey.getFullPath().startsWith(Config.CONFIG_PATH)) {
            // Edit config -> toggle boolean or do nothing for others -> check text
            Config config = Config.fromKeyOrNull(entryKey.nodeId);
            if (null == config) {
                // may browse value - so config is parent
                config = Config.fromKeyOrNull(entryKey.topic);
                if (null == config) {
                    logger.error("Config entry {} not found!", entryKey.getFullPath());
                    return true; // indicate the click was handled
                } else {
                    config.setValue(entryKey.nodeId); // nodeId is the config value - like Tenant-Id
                }
            } else if (config.getDefaultValue() instanceof Boolean) {
                logger.info("Toggle config entry for {} ('{}').", entryKey.getFullPath(),getText());
                config.toggleValue(); // nodeId is the config key
                // TODO just update button text - merge with MainItemAdapter-Logic!
                setText( Config.DisplayName(entryKey.nodeId) + ": " + Entries.getEntry(entryKey).content );
            } else if (Config.TENANT.is(config) || config.name().startsWith("TEST_") ) {
                logger.warn("Edit Config {} by click.", entryKey.getFullPath());
                Entries.setCurrentEntryKey(entryKey);
                MainActivity.switchToInputFragment(); // navigate to edit this entry
            } else {
                logger.error("FAIL - Edit config entry {} not allowed!", entryKey.getFullPath());
                MainActivity.userInfo("Your are not allowed to edit this config entry.");
            }
        } else {
            logger.info("Do not enter leaf node (no children for {}).", entryKey.getFullPath());
        }
        return true; // indicate the click was handled
    }

    @Override
    public boolean performLongClick() {
        if (moveStarted){
            return true; // ignore long-click if movement was started
        }
        super.performLongClick();
        if (null == entryKey) {
            logger.error("ButtonTouchable is not initialized!");
            return true; // indicate the click was handled
        }
        assert entryKey != null;
        logger.info("ButtonTouchable long-clicked.");
        UtilDebug.logCompactCallStack();
        resetBackgroundAfterDelay();
        Entries.setCurrentEntryKey(entryKey);
        MainActivity.switchToInputFragment(); // navigate to edit this entry
        // Custom behavior can be added here
        return super.performLongClick();
    }


    private void onDoubleClick() {
        logger.info("ButtonTouchable double-clicked.");
    }
    private void onSwipeLeft() {
        logger.info("ButtonTouchable swiped left.");
//        setColor(getResources().getColor(android.R.color.holo_red_light)); // Swipe left feedback
        resetBackgroundAfterDelay();
        Entries.vote( entryKey, -1); // down vote
    }

    private void onSwipeRight() {
        logger.info("ButtonTouchable swiped right.");
//        setColor(getResources().getColor(android.R.color.holo_green_light)); // Swipe right feedback
        resetBackgroundAfterDelay();
        Entries.vote( entryKey, 1); // upvote
    }

    private void resetBackgroundAfterDelay() {
//        if (true){
//            return;
//        }
        new Handler().postDelayed(() -> {
            if (null == background){
                logger.error("ButtonTouchable resetBackgroundAfterDelay: background is null!");
                return;
            }
            logger.debug("ButtonTouchable resetBackgroundAfterDelay: restoring background.");
            setBackground(background);
            // leave a white background ;-( ... just WORKAROUND for now
            setBackgroundTintList( getContext().getColorStateList(R.color.transparent) );
            setBackgroundTintMode(android.graphics.PorterDuff.Mode.SRC_IN);


//            setBackground(null);
//            setBackgroundTintList(null);
//            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
//            setBackgroundColor( getContext().getColor(R.color.white) );
//            setBackgroundTintList( getContext().getColorStateList(R.color.transparent) );
//            setBackgroundTintMode(android.graphics.PorterDuff.Mode.SRC_IN);
//
            //setBackground(background);
            // remove border
            // remove drawable

        }, 300);
    }


    /*
        color
        - onTouch
        - onMove - left/right
        - reset

     */
    private void setColor(int colorId) {
        //getBackground().setColorFilter(colorId, PorterDuff.Mode.SRC_ATOP);
        int colorIdWhite = ContextCompat.getColor(MainActivity.getInstance(), R.color.white);
        GradientDrawable gradientDrawable = new GradientDrawable();
        //gradientDrawable.setColor(colorIdWhite); // Background color
        /*
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, // Gradient direction
                new int[]{Color.RED, Color.GREEN} // Colors for the gradient
        );

         */
        gradientDrawable.setStroke(4, colorId); // Border width and color
        gradientDrawable.setCornerRadius(32); // Optional: Rounded corners
        setBackground(null);
        setBackground(gradientDrawable);
    }


}
