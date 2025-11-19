package com.example.fayf_android002;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Configuration {

    ENABLE_NOTIFICATIONS("enable_notifications", true),
    SHOW_LOGS("show_logs", false),
    DARK_MODE("dark_mode", false),
    AUTO_SYNC("auto_sync", true),
    LANGUAGE("language", "en");

    private final String key;
    private final Object defaultValue;

    Configuration(String key, Object defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public static Configuration fromKey(String key) {
        for (Configuration setting : values()) {
            if (setting.getKey().equals(key)) {
                return setting;
            }
        }
        throw new IllegalArgumentException("Unknown setting key: " + key);
    }

    /*
        Instance methods
    */

    public String getValue() {
        return Configuration.get(this.key);
    }

    public boolean asBoolean() {
        return Util.asBoolean(Configuration.get(this.key));
    }

    public void setValue(String value) {
        Configuration.set(this.key, value);
    }

    public void setValue(boolean value) {
        Configuration.set(this.key, Boolean.toString(value));
    }

    public void toggleValue() {
        Configuration.toggle(this.key);
    }


    /*
        Configuration static methods
    */


    static Logger logger = LoggerFactory.getLogger(Configuration.class);

    public final static String CONFIG_PATH = "/_/config";  // HIDDEN_ENTRY_PATH + "/config"


    // TODO use Resource IDs instead of hardcoded strings
    // define enumeration of settings entries


    public static void set(String key, String value) {
        Entry entry = Entries.getEntryOrNew(CONFIG_PATH, key, value);
        // todo - check if created or updated
        if (!entry.getContent().equals(value)) {
            logger.info("configuration set '{}' to '{}'", key, value);
            entry.setContent(value);
        }
    }

    public static String get(String key) {
        Entry entry = Entries.getEntryOrNew(CONFIG_PATH, key, "");
        logger.info("configuration read '{}' with value '{}'", key, entry.getContent());
        return entry.getContent();
    }

    public static String toggle(String key) {
        Entry entry = Entries.getEntryOrNew(CONFIG_PATH, key, "false");
        String newValue = entry.getContent().equals("true") ? "false" : "true";
        entry.setContent(newValue);
        logger.info("configuration toggled '{}' to '{}'", key, newValue);
        return newValue;
    }


}
