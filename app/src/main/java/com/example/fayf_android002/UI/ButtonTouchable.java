package com.example.fayf_android002.UI;

import android.view.MotionEvent;
import com.example.fayf_android002.Config;
import com.example.fayf_android002.Entry.Entries;
import com.example.fayf_android002.Entry.EntryKey;
import com.example.fayf_android002.FirstFragment;
import com.example.fayf_android002.MainActivity;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import com.google.android.material.button.MaterialButton;
import org.slf4j.Logger;

public class ButtonTouchable extends MaterialButton {

    Logger logger = org.slf4j.LoggerFactory.getLogger(ButtonTouchable.class);

    private EntryKey entryKey = null; // entry associated with this button
    private FirstFragment fragment = null; // fragment containing this button

    OnTouchListener touchListener = null;

    // constructor with context, attribute set
    public ButtonTouchable(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
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
    public void setOnTouchListener(OnTouchListener listener) {
        this.touchListener = listener;
        super.setOnTouchListener(listener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (touchListener != null) {
            // forward to CustomOnTouchListener
            touchListener.onTouch(this, event);
        }
        return super.onTouchEvent(event);
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
                fragment.updateButtonsUIThread();
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

}
