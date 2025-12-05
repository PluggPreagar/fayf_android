package com.example.fayf_android002.UI;

import com.example.fayf_android002.Config;
import com.example.fayf_android002.Entry.Entries;
import com.example.fayf_android002.Entry.Entry;
import com.example.fayf_android002.Entry.EntryKey;
import com.example.fayf_android002.FirstFragment;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import com.google.android.material.button.MaterialButton;
import org.slf4j.Logger;

public class ButtonTouchable extends MaterialButton {

    Logger logger = org.slf4j.LoggerFactory.getLogger(ButtonTouchable.class);

    private EntryKey entryKey = null; // entry associated with this button
    private FirstFragment fragment = null; // fragment containing this button

    com.example.fayf_android002.OnTouchListener touchListener = null;

    // constructor with context, attribute set
    public ButtonTouchable(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
    }


    public void setEntry(EntryKey entryKey, FirstFragment fragment) {
        this.entryKey = entryKey;
        this.fragment = fragment;
    }

    public ButtonTouchable(android.content.Context context) {
        super(context);
    }


    public void setTouchListener(com.example.fayf_android002.OnTouchListener listener) {
        this.touchListener = listener; // access for test
        super.setOnTouchListener(touchListener);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        UtilDebug.logCompactCallStack("ButtonTouchable performClick");
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
            } else {
                logger.info("No toggle action for non-boolean config entry {}.", entryKey.getFullPath());
            }
        } else {
            logger.info("Do not enter leaf node (no children for {}).", entryKey.getFullPath());
        }
        return true; // indicate the click was handled
    }

    @Override
    public boolean performLongClick() {
        super.performLongClick();
        logger.info("ButtonTouchable long-clicked.");
        fragment.navigateToEdit(entryKey); // navigate to edit this entry
        // Custom behavior can be added here
        return super.performLongClick();
    }

}
