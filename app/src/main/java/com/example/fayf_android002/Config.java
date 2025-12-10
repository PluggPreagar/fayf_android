package com.example.fayf_android002;

import com.example.fayf_android002.Entry.Entries;
import com.example.fayf_android002.Entry.Entry;
import com.example.fayf_android002.Entry.EntryKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Config {

    AUTO_SYNC_YN("auto_sync_YN", true),
    DARK_MODE_YN("dark_mode_YN", false),
    ENABLE_NOTIFICATIONS_YN("enable_notifications_YN", true),
    LANGUAGE("language", "en"),
    RUN_SELF_TEST("self_test", false),
    SHOW_LOGS("show_logs_0to3", 0),
    SYSTEM("system", "sid_example"),
    TEST_STRING("test_string", "default_value"),
    TENANT("tenant", "tst"),

    VERSION("version", "1.0.0"),



    ;

    static Logger logger = LoggerFactory.getLogger(Config.class);

    public final static String CONFIG_PATH = "/_/config";  // HIDDEN_ENTRY_PATH + "/config"
    public final static String TENANT_TEST_SUFFIX = "Test";

    private final String key;
    private final Object defaultValue;
    private Object value;

    Config(String key, Object defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }



    public String getKey() {
        return key;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public static Config fromKey(String key) {
        for (Config setting : values()) {
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
        return Config.get(this.key);
    }

    public boolean asBoolean() {
        return Util.asBoolean(Config.get(this.key));
    }

    public void setValue(String value) {
        Config.set(this.key, value);
    }


    public void toggleValue() {
        Config.toggle(this.key);
    }


    /*
        Configuration static methods
    */


    // TODO use Resource IDs instead of hardcoded strings
    // define enumeration of settings entries


    public static void set(String key, String value) {
        Config configChanged = Config.fromKey(key);// validate key
        if (configChanged.value.equals(value)) {
            logger.debug("Config '{}' unchanged with value '{}'", key, value);
            return; // no change
        }
        configChanged.value = value; // use instance method
        Entries.setEntry(new EntryKey(CONFIG_PATH, key), value, null);
        logger.info("Config set '{}' to value '{}'", key, value);
        if (configChanged.is(TENANT)) {
            // reset Entries to apply new tenant, and reload all entries
            logger.warn("Config TENANT changed - reloading all entries for new tenant '{}'", value);
            Entries.resetEntries();
            Entries.load_async(MainActivity.getInstance());
        }
    }

    public static String get(String key) {
        Config config = Config.fromKey(key);// validate key
        String content = Entries.getContentOr(CONFIG_PATH, key, String.valueOf(config.value)); // validate key
        if (content.isEmpty()) {
            logger.warn("Config '{}' is empty, using default value '{}'", key, config.getDefaultValue());
            content = String.valueOf(config.getDefaultValue());
        }
        logger.info("Config read '{}' with value '{}'", key,content);
        return content;
    }

    public static String toggle(String key) {
        Config config = Config.fromKey(key);// validate key
        EntryKey currentTopicEntry = new EntryKey(CONFIG_PATH, key);
        Entry entry = Entries.getEntry(currentTopicEntry); // validate key
        String newValue = toggle(key, null != entry ? entry.getContent() : null, config.getDefaultValue());
        Entries.setEntry(currentTopicEntry, newValue, null);
        logger.info("Config toggled '{}' to '{}'", key, newValue);
        return newValue;
    }


    public static String toggle(String key, String currentValue, Object defaultValue) {
        String newValue = "";
        if (key.endsWith("_YN") || defaultValue instanceof Boolean) {
            newValue = (null != currentValue ? Util.asBoolean(currentValue)
                        : Util.asBoolean(String.valueOf(defaultValue)))
                    ? "false" : "true";
        } else if (defaultValue instanceof Integer
                || key.contains("to") && key.replaceAll("[0-9]+","i").endsWith("_itoi")) {
            // extract int values from key like int_0to3 or int_1to5
            String[] split = key.replaceAll(".*_", "").split("to");
            int lowerBound = 2 == split.length ? Util.parseIntOr(split[0], 0) : 0;
            int upperBound = 2 == split.length ? Util.parseIntOr(split[1], 3) : 3;
            int intValue = defaultValue instanceof Integer ? (Integer) defaultValue : lowerBound;
            try {
                intValue = Integer.parseInt(currentValue);
            } catch (Exception e) {
                // ignore
            }
            intValue = (intValue + 1) % (upperBound  + 1); // cycle 0 to 3
            newValue = String.valueOf(intValue);
        } else {
            logger.warn("Config toggle not supported for key '{}' with current value '{}'", key, currentValue);
            newValue = currentValue != null ? currentValue : String.valueOf(defaultValue);

        }
        return newValue;
    }


    public static String DisplayName(String nodeId) {
        return nodeId
                .replaceFirst("_YN$", "")
                .replaceFirst("_[0-9]+to[0-9]+$", "");
    }


    public boolean is(Config config) {
        return null != config && this.name().equals(config.name());
    }
}
