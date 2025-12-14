package com.example.fayf_android002.Entry;

import com.example.fayf_android002.R;

public enum EntryStyle {

    NOTE(".", "Note", R.drawable.ic_baseline_note_24, false),
    FOLDER(">", "Folder", R.drawable.baseline_chevron_right_24, false),
    FACT ("!", "Fact", R.mipmap.icons8_checked_100_2, true),
    FALSE_FACT ("!-", "False Fact", R.mipmap.icons8_radioactive_100, false),
    QUESTION ("?", "Question", R.mipmap.icons8_ask_question_100, true),
    COUNTER_QUESTION("??", "Counter Question", R.mipmap.icons8_answer_100, false),
    REFERENCE ("@", "Reference", R.mipmap.icons8_book_100, true);

    private final String suffix;
    private final String description;
    private final int iconResourceId;
    private final boolean supportsVoting;

    EntryStyle(String suffix, String description, int icons8Book100, boolean b) {
        this.suffix = suffix;
        this.description = description;
        this.iconResourceId = icons8Book100;
        this.supportsVoting = b;
    }

    static EntryStyle getBySuffix(String suffix) {
        for (EntryStyle style : EntryStyle.values()) {
            if (style.getSuffix().equals(suffix)) {
                return style;
            }
        }
        throw new IllegalArgumentException("Unknown style suffix: " + suffix);
    }

    public static EntryStyle getByContent(String content) {
        EntryStyle styleResult = NOTE;
        for (EntryStyle style : EntryStyle.values()) {
            // styles are checked in declaration order, so more specific suffixes should come later
            if (content.endsWith(style.getSuffix())) {
                styleResult = style;
            }
        }
        return styleResult; // default style
    }


    public String getSuffix() {
        return suffix;
    }
    public String getDescription() {
        return description;
    }
    public int getIconResourceId() {
        return iconResourceId;
    }
    public boolean supportsVoting() {
        return supportsVoting;
    }


}
