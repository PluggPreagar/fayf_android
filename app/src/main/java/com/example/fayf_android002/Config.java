package com.example.fayf_android002;

import com.example.fayf_android002.Entry.*;
import com.example.fayf_android002.RuntimeTest.RuntimeChecker;
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

    COMMIT_HASH("commit_hash", BuildConfig.GIT_COMMIT_HASH),

    FULL_MENU_YN("extended_settings_YN", false),

    SHOW_FACT_YN("show_fact_YN", true),
    SHOW_FALSE_FACT_YN("show_false_fact_YN", false),
    SHOW_QUESTION_YN("show_question_YN", false),
    SHOW_COUNTER_QUESTION_YN("show_counter_question_YN", false),
    SHOW_REFERENCE_YN("show_reference_YN", false),

    LAST_SYNC_TIMESTAMP("last_sync_timestamp", ""),
    LAST_DATA_TIMESTAMP("last_data_timestamp", "")

    ;

    //
    static List<String> fixedValues = getFixedValues();


    static Logger logger = LoggerFactory.getLogger(Config.class);

    public final static String CONFIG_PATH = "/_/config";  // HIDDEN_ENTRY_PATH + "/config"
    public final static String TENANT_TEST_SUFFIX = "Test";

    private final String key;
    private final Object defaultValue;

    Config(String key, Object defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
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
        // separate handling for version and build_time
        if (fixedValues.contains(key)) {
            logger.info("Config '{}': '{}' (fix)", key, defaultValue);
            return defaultValue.toString();
        }
        String content = Entries.getContentOr(CONFIG_PATH, key, String.valueOf(defaultValue)); // validate key
        if (content.isEmpty()) {
            logger.warn("Config '{}': '{}' (is empty, using default value)", key, getDefaultValue());
            content = String.valueOf(getDefaultValue());
        } else {
            logger.info("Config '{}': '{}'", key,content);
        }
        return content;
    }

    public boolean getBooleanValue() {
        return Util.asBoolean(getValue());
    }

    public void toggleValue() {
        Config.toggle(this.key);
    }


    /*
        Configuration static methods
    */



    public void setValue(String newValue) {
        RuntimeChecker.check();
        String oldValue = getValue();
        //
        String value = newValue.trim().replaceAll("\\s*>\\s*$",""); // KLUDGE to remove trailing > added by Entries-Topic
        if (!value.equals(newValue)) {
            logger.warn("Config '{}' value trimmed from '{}' to '{}'", key, newValue, value);
        }
        if (fixedValues.contains(key)) {
            if (oldValue.equals(value)) {
                logger.debug("Config '{}' unchanged with fixed value '{}'", key, value);
            } else {
                logger.warn("Config '{}' is fixed (keep  '{}', ignore new value '{}')"
                        , key, oldValue, value);
            }
        } else if (oldValue.equals(value)) {
            logger.debug("Config '{}' unchanged with value '{}'", key, value);
        } else {
            // apply change
            EntryKey entryKey = new EntryKey(CONFIG_PATH, key);
            Entries.setEntry(entryKey, value, null);
            logger.info("Config set '{}' to value '{}' (was '{}')", key, value, oldValue);
            if (name().equals(TENANT.name())) {
                switchTenant(entryKey, value, oldValue);
            }
            // save config change immediately
            if (!name().startsWith("LAST_")) {
                // skipp last_sync_timestamp and last_data_timestamp to reduce IO, without any other change
                Entries.save(MainActivity.getContext(), entryKey);
            }
            // Post-Action for specific configs
            if (name().equals(TENANT.name())) {
                // asynchronous reload of all entries for new tenant
                MainActivity.userInfo("Tenant changed " + value.replaceFirst(".*:", ""));
                MainActivity.switchToFirstFragment(); // will reload entries on becoming visible
            }
        } // change allowed ?
    }

    private static void switchTenant(EntryKey entryKey, String value, String oldValue) {
        // reset Entries to apply new tenant, and reload all entries
        logger.info("----------------------------------------------");
        logger.info("Switching tenant to '{}' --------------", value);
        logger.warn("Config TENANT changed - '{}'", value);
        logger.warn("Config TENANT changed - save old config+data, reload data'{}'", value);
        // tricky ... add list of tenants? -> make tenant a topic to choose from?
        // should have tenant id and name -> but where to store/share name?
        // WO combine ... <tenant_id>:<tenant_name>
        Entries.setEntry(new EntryKey(entryKey.getFullPath(), value), value.replaceFirst(".*:", ""), null);
        Entries.setEntry(new EntryKey(entryKey.getFullPath(), oldValue), oldValue.replaceFirst(".*:", ""), null); // make sure there is way back
        // show available tenants
        SortedEntryMap topic = Entries.entryTree.getTopic(entryKey);
        logger.info("Tenants available '{}':", topic.size());
        topic.forEach((k, v) -> logger.debug("    Tenant entry: {} => {}", k, v.getContent()));
        //
        Entries.setCurrentEntryKey(null); // reset current entry
        Entries.resetEntries(true);
    }




    public static String toggle(String key) {
        Config config = Config.fromKey(key);// validate key
        EntryKey currentTopicEntry = new EntryKey(CONFIG_PATH, key);
        Entry entry = Entries.getEntry(currentTopicEntry); // validate key
        String newValue = toggle(key, null != entry ? entry.getContent() : null, config.getDefaultValue());
        Entries.setEntry(currentTopicEntry, newValue, null);
        logger.info("Config '{}': '{}' (toggled)", key, newValue);
        Entries.save(MainActivity.getContext(), currentTopicEntry); // save immediately
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




}
