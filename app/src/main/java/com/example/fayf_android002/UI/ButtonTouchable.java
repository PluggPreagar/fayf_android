package com.example.fayf_android002.UI;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
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

    private GestureDetector gestureDetector;


    // constructor with context, attribute set
    public ButtonTouchable(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                performClick(); // Handle single click
                return true;
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
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                background = getBackground();
                setBackgroundColor(getResources().getColor(android.R.color.darker_gray)); // Light feedback on touch start

                // Set the red trash bin icon on the right side of the button
                Drawable trashBinIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_folder_24);
                if (trashBinIcon != null) {
                    trashBinIcon.setBounds(0, 0, trashBinIcon.getIntrinsicWidth(), trashBinIcon.getIntrinsicHeight());
                }
                setCompoundDrawablesWithIntrinsicBounds(null, null, trashBinIcon, null);

                // Optional: Add padding between the text and the icon
                setCompoundDrawablePadding(16);

                break;

            case MotionEvent.ACTION_MOVE:
                // Detect swipe direction dynamically
                float deltaX = event.getX() - startX;
                float maxSwipeDistance = getWidth(); // Use the button's width as the max swipe distance
                float proportion = Math.min(1, Math.abs(deltaX) / maxSwipeDistance); // Clamp proportion to [0, 1]

                // interpolate middle color between red and green based on swipe direction and distance
                // deltaXtouch starting from real position
                float deltaXtouch = event.getX() - startX;
                int middleColor = deltaX < 0
                        ? Color.rgb(255, (int)(255 * (proportion)), (int)(255 * (proportion))) // Red to white
                        : Color.rgb((int)(255 * (proportion)), 255, (int)(255 * (proportion))); // White to green

                // Calculate gradient shift
                int[] colors = new int[]{Color.RED, middleColor, Color.GREEN};
                float[] positions = new float[]{Math.max(0, 0.5f - proportion / 2), Math.min(1, 0.5f + proportion / 2)};

                GradientDrawable gradientDrawable = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT, colors
                );
                //gradientDrawable.setGradientCenter( positions[0], positions[1]);
                gradientDrawable.setCornerRadius(32); // Optional: Rounded corners
                gradientDrawable.setStroke(4, Color.BLACK); // Optional: Border width and color

                setBackground(gradientDrawable); // Apply the gradient dynamically

                //



                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
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
        super.performClick();
        UtilDebug.logCompactCallStack("ButtonTouchable performClick");
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
            Config config = Config.fromKey(entryKey.nodeId);
            if (config.getDefaultValue() instanceof Boolean) {
                logger.info("Toggle config entry for {}.", entryKey.getFullPath());
                config.toggleValue(); // nodeId is the config key
                // force refresh button text - refresh fragment
                // fragment.updateButtonsUIThread();
            } else if (Config.TENANT.is(config) || config.name().startsWith("TEST_") ) {
                logger.warn("Edit Config {} by click.", entryKey.getFullPath());
                fragment.navigateToEdit(entryKey); // navigate to edit this entry
            } else {
                logger.error("FAIL - Edit config entry {} not allowed!", entryKey.getFullPath());
                MainActivity.notifyUser("Your are not allowed to edit this config entry.");
            }
        } else {
            logger.info("Do not enter leaf node (no children for {}).", entryKey.getFullPath());
        }
        return true; // indicate the click was handled
    }

    @Override
    public boolean performLongClick() {
        super.performLongClick();
        if (null == entryKey) {
            logger.error("ButtonTouchable is not initialized!");
            return true; // indicate the click was handled
        }
        assert entryKey != null;
        logger.info("ButtonTouchable long-clicked.");
        UtilDebug.logCompactCallStack();
        fragment.navigateToEdit(entryKey); // navigate to edit this entry
        // Custom behavior can be added here
        return super.performLongClick();
    }


    private void onDoubleClick() {
        logger.info("ButtonTouchable double-clicked.");
    }
    private void onSwipeLeft() {
        logger.info("ButtonTouchable swiped left.");
        setColor(getResources().getColor(android.R.color.holo_red_light)); // Swipe left feedback
        resetBackgroundAfterDelay();
    }

    private void onSwipeRight() {
        logger.info("ButtonTouchable swiped right.");
        setColor(getResources().getColor(android.R.color.holo_green_light)); // Swipe right feedback
        resetBackgroundAfterDelay();
    }

    private void resetBackgroundAfterDelay() {
        new Handler().postDelayed(() -> {
            setColor(getResources().getColor(android.R.color.transparent));
            setBackground(background);
        }, 300);
    }


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
