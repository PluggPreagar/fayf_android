package com.example.fayf_android002.UI;

import com.example.fayf_android002.Entries;
import com.example.fayf_android002.Entry;
import com.example.fayf_android002.FirstFragment;
import com.google.android.material.button.MaterialButton;
import org.slf4j.Logger;

public class ButtonTouchable extends MaterialButton {

    Logger logger = org.slf4j.LoggerFactory.getLogger(ButtonTouchable.class);

    private Entry entry = null; // entry associated with this button
    private FirstFragment fragment = null; // fragment containing this button

    com.example.fayf_android002.OnTouchListener touchListener = null;

    // constructor with context, attribute set
    public ButtonTouchable(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
    }


    public void setEntry(Entry entry, FirstFragment fragment) {
        this.entry = entry;
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
        logger.info("ButtonTouchable clicked.");
        Entries.setTopicEntry(entry); // set topic to this entry
        return true; // indicate the click was handled
    }

    @Override
    public boolean performLongClick() {
        super.performLongClick();
        logger.info("ButtonTouchable long-clicked.");
        fragment.navigateToEdit(entry); // navigate to edit this entry
        // Custom behavior can be added here
        return super.performLongClick();
    }

}
