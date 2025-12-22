package com.example.fayf_android002;

import com.example.fayf_android002.Entry.Entries;
import com.example.fayf_android002.Entry.Entry;
import com.example.fayf_android002.Entry.EntryKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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

    VERSION("version", BuildConfig.VERSION_NAME),
    BUILD_TIME("build_time", Util.convertTime(Long.parseLong(BuildConfig.BUILD_TIME))),

    COMMIT_HASH("commit_hash", BuildConfig.GIT_COMMIT_HASH)

    ;

    //
    static List<String> fixedValues = getFixedValues();


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

    private static List<String> getFixedValues() {
        List<String> fixed = new ArrayList<>();
        fixed.add(VERSION.key);
        fixed.add(BUILD_TIME.key);
        fixed.add(COMMIT_HASH.key);
        return fixed;
    }


    public String getKey() {
        return key;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public static Config fromKey(String key) {
        if (null == key || key.isEmpty()) {
            throw new IllegalArgumentException("Config key cannot be null or empty");
        }
        String shortKey = stripConfigPath(key);
        for (Config setting : values()) {
            if (setting.getKey().equals(shortKey)) {
                return setting;
            }
        }
        throw new IllegalArgumentException("Unknown setting key: " + key+ " (stripped: " + shortKey + ")");
    }

    public static Config fromKeyOrNull(String key) {
        if (null == key || key.isEmpty()) {
            return null;
        }
        String shortKey = stripConfigPath(key);
        for (Config setting : values()) {
            if (setting.getKey().equals(shortKey)) {
                return setting;
            }
        }
        return null;
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
        if (fixedValues.contains(key)) {
            if (configChanged.value.equals(value)) {
                logger.debug("Config '{}' unchanged with fixed value '{}'", key, value);
                return; // no change
            }
            logger.warn("Config '{}' is fixed (keep  '{}', ignore new value '{}')"
                    , key, configChanged.value, value);
            return; // no change
        }
        if (configChanged.value.equals(value)) {
            logger.debug("Config '{}' unchanged with value '{}'", key, value);
            return; // no change
        }
        String oldValue = String.valueOf(configChanged.value);
        configChanged.value = value; // use instance method
        EntryKey entryKey = new EntryKey(CONFIG_PATH, key);
        Entries.setEntry(entryKey, value, null);
        logger.info("Config set '{}' to value '{}' (was '{}')", key, value, oldValue);
        if (configChanged.is(TENANT)) {
            // reset Entries to apply new tenant, and reload all entries
            logger.warn("Config TENANT changed - reloading all entries for new tenant '{}'", value);
            Entries.resetEntries();
            Entries.load_async(MainActivity.getInstance());
            MainActivity.getInstance().switchToFirstFragment(); // navigate to edit this entry
            Entries.rootTopic();
            // tricky ... add list of tenants? -> make tenant a topic to choose from?
            // should have tenant id and name -> but where to store/share name?
            // WO combine ... <tenant_id>:<tenant_name>
            saveForSelection(entryKey, value);
            saveForSelection(entryKey, oldValue); // make sure there is way back
        }
    }


    private static void saveForSelection(EntryKey parentEntryKey, String idAndValue) {
        String name = idAndValue;
        if (idAndValue.contains(":")) {
            String[] split = idAndValue.split(":", 2);
            String id = split[0];
            name = split[1];
            MainActivity.getInstance().userInfo("Tenant changed to: " + name + " (" + id + ")");
        } else {
            MainActivity.getInstance().userInfo("Tenant changed to: " + idAndValue);
        }
        Entries.setEntry(new EntryKey(parentEntryKey.getFullPath(), idAndValue), idAndValue, null);
    }

    public static String get(String key) {
        Config config = Config.fromKey(key);// validate key
        // separate handling for version and build_time
        if (fixedValues.contains(key)) {
            logger.info("Config read '{}' with value '{}' (fix)", key, String.valueOf(config.value));
            return String.valueOf(config.value);
        }
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

        return stripConfigPath(nodeId)
                .replaceFirst("_YN$", "")
                .replaceFirst("_[0-9]+to[0-9]+$", "");
    }

    public static String stripConfigPath(String fullPath) {
        if (fullPath.startsWith(CONFIG_PATH + "/")) {
            return fullPath.substring((CONFIG_PATH + "/").length());
        } else {
            return fullPath;
        }
    }


    public boolean is(Config config) {
        return null != config && this.name().equals(config.name());
    }


    public void save(){

    }

}
