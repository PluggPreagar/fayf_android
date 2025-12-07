package com.example.fayf_android002;

import junit.framework.TestCase;
import org.junit.Test;

public class ConfigTest extends TestCase {

    public void checkToggle(String description, Object expectedValue
            , String key, String currentValue, Object defaultValue) {
        assertEquals(description, String.valueOf(expectedValue)
                , Config.toggle(key, currentValue, defaultValue) );
    }

    public void testToggle() {

        checkToggle("Toggle true to false", false
                , "test_YN", "true", true);
        checkToggle("Toggle false to true", true
                , "test_YN", "false", true);

        checkToggle("Non-boolean remains unchanged", "some_string"
                , "test_string", "some_string", "default_value");

        checkToggle("int_0to3 cycles 0->1", 1
                , "int_0to3", "0", 0);
        checkToggle("int_0to3 cycles 1->2", 2
                , "int_0to3", "1", 0);
        checkToggle("int_0to3 cycles 2->3", 3
                , "int_0to3", "2", 0);
        checkToggle("int_0to3 cycles 3->0", 0
                , "int_0to3", "3", 0);
    }
}